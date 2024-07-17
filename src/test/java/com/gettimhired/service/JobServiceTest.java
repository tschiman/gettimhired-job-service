package com.gettimhired.service;

import com.gettimhired.TestHelper;
import com.gettimhired.error.APIUpdateException;
import com.gettimhired.model.dto.JobDTO;
import com.gettimhired.model.dto.update.JobUpdateDTO;
import com.gettimhired.model.mongo.Job;
import com.gettimhired.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.gettimhired.TestHelper.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class JobServiceTest {

    private JobService jobService;
    private JobRepository jobRepository;

    @BeforeEach
    public void init() {
        jobRepository = mock(JobRepository.class);
        jobService = new JobService(jobRepository);
    }

    @Test
    public void testfindAllJobsForUserAndCandidateIdHappy() {
        var e1 = getJob("BARK_NAME");
        var e2 = getJob("BARK_NAME_TWO");
        var jobs = List.of(e1, e2);
        when(jobRepository.findAllByUserIdAndCandidateId(TestHelper.USER_ID, TestHelper.CANDIDATE_ID)).thenReturn(jobs);

        var result = jobService.findAllJobsForUserAndCandidateId(TestHelper.USER_ID, TestHelper.CANDIDATE_ID);

        verify(jobRepository, times(1)).findAllByUserIdAndCandidateId(TestHelper.USER_ID, TestHelper.CANDIDATE_ID);
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    @Test
    public void testFindJobByUserIdAndCandidateIdAndId_Found() {

        var job = getJob("BARK_NAME");
        var expectedJobDTO = new JobDTO(job);
        when(jobRepository.findJobByIdAndUserId(anyString(), anyString()))
                .thenReturn(Optional.of(job));

        Optional<JobDTO> result = jobService.findJobByIdAndUserId(ID, USER_ID);

        assertTrue(result.isPresent());
        assertEquals(expectedJobDTO, result.get());
    }

    @Test
    public void testFindJobByUserIdAndCandidateIdAndId_NotFound() {

        when(jobRepository.findJobByIdAndUserId(anyString(), anyString()))
                .thenReturn(Optional.empty());

        Optional<JobDTO> result = jobService.findJobByIdAndUserId(ID, USER_ID);

        assertFalse(result.isPresent());
    }

    @Test
    public void testCreateJob_Success() {
        var job = getJob("BARK_NAME");;
        JobDTO jobDTO = new JobDTO(job);
        Job savedJob = getJob("BARK_NAME"); // Assuming you have an Job entity
        when(jobRepository.save(any(Job.class))).thenReturn(savedJob);

        Optional<JobDTO> result = jobService.createJob(TestHelper.USER_ID, TestHelper.CANDIDATE_ID, jobDTO);

        assertTrue(result.isPresent());
        assertEquals(new JobDTO(savedJob), result.get());
    }

    @Test
    public void testCreateJob_Failure() {
        JobDTO jobDTO = new JobDTO(getJob("BARK_NAME"));
        when(jobRepository.save(any(Job.class))).thenThrow(new RuntimeException("Database error"));

        Optional<JobDTO> result = jobService.createJob(TestHelper.USER_ID, TestHelper.CANDIDATE_ID, jobDTO);

        assertFalse(result.isPresent());
    }

    @Test
    public void testUpdateJob_JobNotFound() {
        var jobUpdateDto = getJobUpdate();
        when(jobRepository.findById(TestHelper.ID)).thenReturn(Optional.empty());

        var ex = assertThrows(APIUpdateException.class, () -> jobService.updateJob(TestHelper.ID, TestHelper.USER_ID, TestHelper.CANDIDATE_ID, jobUpdateDto));

        verify(jobRepository, times(1)).findById(TestHelper.ID);
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
    }

    @Test
    public void testUpdateJob_UserIdNotMatch() {
        var job = getJob("BARK_NAME");
        var jobUpdateDto = getJobUpdate();
        when(jobRepository.findById(TestHelper.ID)).thenReturn(Optional.of(job));

        var ex = assertThrows(APIUpdateException.class, () -> jobService.updateJob(TestHelper.ID, "NO_MATCH", TestHelper.CANDIDATE_ID, jobUpdateDto));

        verify(jobRepository, times(1)).findById(TestHelper.ID);
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
    }

    @Test
    public void testUpdateJob_CandidateIdNotMatch() {
        var job = getJob("BARK_NAME");
        var jobUpdateDto = getJobUpdate();
        when(jobRepository.findById(TestHelper.ID)).thenReturn(Optional.of(job));

        var ex = assertThrows(APIUpdateException.class, () -> jobService.updateJob(TestHelper.ID, TestHelper.USER_ID, "NO_MATCH", jobUpdateDto));

        verify(jobRepository, times(1)).findById(TestHelper.ID);
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
    }

    @Test
    public void testUpdateJob_SaveThrowsException() {
        var job = getJob("BARK_NAME");
        var jobUpdateDto = getJobUpdate();
        when(jobRepository.findById(TestHelper.ID)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenThrow(new RuntimeException());

        var result = jobService.updateJob(TestHelper.ID, TestHelper.USER_ID, TestHelper.CANDIDATE_ID, jobUpdateDto);

        verify(jobRepository, times(1)).findById(TestHelper.ID);
        verify(jobRepository, times(1)).save(any(Job.class));
        assertFalse(result.isPresent());
    }

    @Test
    public void testUpdateJob_Happy() {
        var job = getJob("BARK_NAME");
        var jobUpdateDto = getJobUpdate();
        when(jobRepository.findById(TestHelper.ID)).thenReturn(Optional.of(job));
        when(jobRepository.save(any(Job.class))).thenReturn(job);

        var result = jobService.updateJob(TestHelper.ID, TestHelper.USER_ID, TestHelper.CANDIDATE_ID, jobUpdateDto);

        verify(jobRepository, times(1)).findById(TestHelper.ID);
        verify(jobRepository, times(1)).save(any(Job.class));
        assertTrue(result.isPresent());
    }

    @Test
    public void testDeleteJob_Success() {
        doNothing().when(jobRepository).deleteByIdAndUserId(TestHelper.ID, TestHelper.USER_ID);

        boolean result = jobService.deleteJob(TestHelper.ID, TestHelper.USER_ID);

        assertTrue(result);
    }

    @Test
    public void testDeleteJob_Failure() {
        doThrow(new RuntimeException("Database error")).when(jobRepository).deleteByIdAndUserId(TestHelper.ID, TestHelper.USER_ID);

        boolean result = jobService.deleteJob(TestHelper.ID, TestHelper.USER_ID);

        assertFalse(result);
    }

    @Test
    public void testFindAllJobsByCandidateId_Sorting() {

        var j1 = new JobDTO(null,null,null,null,null,null,LocalDate.of(2000,1,1),null,null,null,null);
        var j2 = new JobDTO(null,null,null,null,null,null,LocalDate.of(2020,1,1),null,null,null,null);
        var j3 = new JobDTO(null,null,null,null,null,null,null,null,null,null,null);
        var jobs = List.of(j1, j2, j3);
        when(jobRepository.findAllByCandidateId(CANDIDATE_ID)).thenReturn(jobs);

        var result = jobService.findAllJobsByCandidateId(CANDIDATE_ID);

        assertEquals(3, result.size());
        assertEquals(j3, result.get(0));
        assertEquals(j2, result.get(1));
        assertEquals(j1, result.get(2));
    }

    private static Job getJob(String name) {
        return new Job(
                UUID.randomUUID().toString(),
                USER_ID,
                CANDIDATE_ID,
                name,
                "BARK_TITLE",
                LocalDate.now(),
                LocalDate.now(),
                new ArrayList<>(),
                new ArrayList<>(),
                true,
                "BARK_LEAVE"
        );
    }

    private JobUpdateDTO getJobUpdate() {
        return new JobUpdateDTO(
                "BARK_NAME",
                "BARK_TITLE",
                LocalDate.now(),
                LocalDate.now(),
                new ArrayList<>(),
                new ArrayList<>(),
                true,
                "BARK_LEAVE"
        );
    }

}