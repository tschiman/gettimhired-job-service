package com.gettimhired.service;


import com.gettimhired.error.APIUpdateException;
import com.gettimhired.model.dto.JobDTO;
import com.gettimhired.model.dto.update.JobUpdateDTO;
import com.gettimhired.model.mongo.Job;
import com.gettimhired.repository.JobRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Service
public class JobService {

    Logger log = LoggerFactory.getLogger(JobService.class);
    private final JobRepository jobRepository;
    private final RestClient resumeSiteRestClient;

    public JobService(JobRepository jobRepository, RestClient resumeSiteRestClient) {
        this.jobRepository = jobRepository;
        this.resumeSiteRestClient = resumeSiteRestClient;
    }

    public List<JobDTO> findAllJobsForUserAndCandidateId(String userId, String candidateId) {
        return jobRepository.findAllByUserIdAndCandidateId(userId, candidateId).stream()
                .map(JobDTO::new)
                .toList();
    }

    public Optional<JobDTO> findJobByIdAndUserId(String id, String userId) {
        return jobRepository.findJobByIdAndUserId(id, userId)
                .map(JobDTO::new);
    }

    public Optional<JobDTO> createJob(String userId, String candidateId, JobDTO jobDto) {
        var job = new Job(userId, candidateId, jobDto);
        try {
            var jobFromDb = jobRepository.save(job);
            var jobFromDatabase = new JobDTO(jobFromDb);
            return Optional.of(jobFromDatabase);
        } catch (Exception e) {
            log.error("createJob userId={} candidateId={}", userId, candidateId, e);
            return Optional.empty();
        }
    }

    public Optional<JobDTO> updateJob(String id, String userId, String candidateId, JobUpdateDTO jobUpdateDTO) {
        //get job from db
        Optional<Job> jobOpt = jobRepository.findById(id);
        if (jobOpt.isPresent()) {
            //check if the username matches
            if (jobOpt.get().userId().equals(userId)) {
                //check the candidateId
                if (jobOpt.get().candidateId().equals(candidateId)) {
                    //then update the candidate values
                    var jobToSave = new Job(
                            jobOpt.get().id(),
                            jobOpt.get().userId(),
                            jobOpt.get().candidateId(),
                            jobUpdateDTO.companyName(),
                            jobUpdateDTO.title(),
                            jobUpdateDTO.startDate(),
                            jobUpdateDTO.endDate(),
                            jobUpdateDTO.skills(),
                            jobUpdateDTO.achievements(),
                            jobUpdateDTO.currentlyWorking(),
                            jobUpdateDTO.reasonForLeaving()
                    );
                    Job jobToReturn;
                    try {
                        jobToReturn = jobRepository.save(jobToSave);
                    } catch (Exception e) {
                        log.error("updateJob userId={} id={} candidateId={}", userId, id, candidateId, e);
                        return Optional.empty();
                    }
                    var jobDto = new JobDTO(jobToReturn);
                    return Optional.of(jobDto);
                } else {
                    throw new APIUpdateException(HttpStatus.FORBIDDEN);
                }
            } else {
                //userId does not match (403)
                throw new APIUpdateException(HttpStatus.FORBIDDEN);
            }
        } else {
            //CandidateId not found(404)
            throw new APIUpdateException(HttpStatus.NOT_FOUND);
        }
    }

    public boolean deleteJob(String id, String userId) {
        try {
            jobRepository.deleteByIdAndUserId(id, userId);
            return true;
        } catch (Exception e) {
            log.error("deleteJob userId={} id={}", userId, id, e);
            return false;
        }
    }

    public List<JobDTO> findAllJobsByCandidateId(String candidateId) {
        return jobRepository.findAllByCandidateId(candidateId)
                .stream().sorted((j1, j2) -> {
                    if (j1.endDate() == null && j2.endDate() == null) {
                        return 0;
                    }
                    if (j1.endDate() == null) {
                        return -1;
                    }
                    if (j2.endDate() == null) {
                        return 1;
                    }
                    return j2.endDate().compareTo(j1.endDate());
                }).toList();
    }

    public void migrateJobs() {
        var jobDtos = resumeSiteRestClient.get()
                .uri("/api/candidates/all/jobs/migrate")
                .retrieve()
                .body(JobDTO[].class);

        if (jobDtos != null) {
            for (JobDTO jobDto : jobDtos) {
                jobRepository.save(new Job(
                        jobDto.id(),
                        jobDto.userId(),
                        jobDto.candidateId(),
                        jobDto.companyName(),
                        jobDto.title(),
                        jobDto.startDate(),
                        jobDto.endDate(),
                        jobDto.skills(),
                        jobDto.achievements(),
                        jobDto.currentlyWorking(),
                        jobDto.reasonForLeaving()
                ));
            }
        }
    }
}
