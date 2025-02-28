package com.gettimhired.controller;

import com.gettimhired.error.APIUpdateException;
import com.gettimhired.model.dto.JobDTO;
import com.gettimhired.model.dto.update.JobUpdateDTO;
import com.gettimhired.service.JobService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@SecurityRequirement(name = "basicAuth")
@RestController
@RequestMapping("/api/candidates/{candidateId}/jobs")
public class JobAPI {

    Logger log = LoggerFactory.getLogger(JobAPI.class);
    private final JobService jobService;

    public JobAPI(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<JobDTO> getAllJobs(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String candidateId,
            @RequestParam String userId
    ) {
        log.info("GET /api/candidates/{candidateId}/jobs getAllJobs userId={} candidateId={}", userId, candidateId);
        return jobService
                .findAllJobsForUserAndCandidateId(
                        userId,
                        candidateId
                );
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JobDTO> getJobById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @PathVariable String candidateId,
            @RequestParam String userId
    ) {
        log.info("GET /api/candidates/{candidateId}/jobs/{id} getAllJobs userId={} candidateId={} id={}", userId, candidateId, id);
        var jobOpt = jobService.findJobByIdAndUserId(id, userId);
        return jobOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JobDTO> createJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid JobDTO jobDTO,
            @PathVariable String candidateId,
            @RequestParam String userId
    ) {
        log.info("POST /api/candidates/{candidateId}/jobs createJob userId={} candidateId={}", userId, candidateId);
        var jobDtoOpt = jobService.createJob(userId, candidateId, jobDTO);
        return jobDtoOpt
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<JobDTO> updateJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody @Valid JobUpdateDTO jobUpdateDTO,
            @PathVariable String id,
            @PathVariable String candidateId,
            @RequestParam String userId
    ) {
        log.info("PUT /api/candidates/{candidateId}/jobs/{id} updateJob userId={} candidateId={} id={}", userId, candidateId, id);
        try {
            var jobDtoOpt = jobService.updateJob(id, userId, candidateId, jobUpdateDTO);
            return jobDtoOpt
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
        } catch (APIUpdateException e) {
            return ResponseEntity.status(e.getHttpStatus()).build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity deleteJob(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable String id,
            @PathVariable String candidateId,
            @RequestParam String userId
    ) {
        log.info("DELETE /api/candidates/{candidateId}/jobs/{id} deleteJob userId={} candidateId={} id={}", userId, candidateId, id);
        boolean result = jobService.deleteJob(id, userId);
        return result ?
                ResponseEntity.ok().build() :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity deleteAllJobsForUserId(
            @PathVariable String candidateId,
            @RequestParam String userId
    ) {
        log.info("DELETE /api/candidates/{candidateId}/jobs deleteAllJobsForUserId userId={} candidateId={}", userId, candidateId);
        boolean result = jobService.deleteAllJobsForCandidateIdAndUserId(candidateId, userId);
        return result ?
                ResponseEntity.ok().build() :
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}
