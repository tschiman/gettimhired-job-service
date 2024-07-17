package com.gettimhired.repository;

import com.gettimhired.model.dto.JobDTO;
import com.gettimhired.model.mongo.Job;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface JobRepository extends MongoRepository<Job, String> {
    List<Job> findAllByUserIdAndCandidateId(String userId, String candidateId);

    Optional<Job> findJobByIdAndUserId(String id, String userId);

    void deleteByIdAndUserId(String id, String userId);

    List<JobDTO> findAllByCandidateId(String candidateId);

    void deleteByUserId(String userId);
}
