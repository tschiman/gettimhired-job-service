package com.gettimhired.config;

import com.gettimhired.model.mongo.Candidate;
import com.gettimhired.model.mongo.ChangeSet;
import com.gettimhired.model.mongo.Education;
import com.gettimhired.model.mongo.Job;
import com.gettimhired.repository.ChangeSetRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.stereotype.Component;

@Component
public class MongoSchemaManager {
    Logger log = LoggerFactory.getLogger(MongoSchemaManager.class);
    private final MongoTemplate mongoTemplate;
    private final ChangeSetRepository changeSetRepository;

    public MongoSchemaManager(MongoTemplate mongoTemplate, ChangeSetRepository changeSetRepository) {
        this.mongoTemplate = mongoTemplate;
        this.changeSetRepository = changeSetRepository;
    }

    @PostConstruct
    public void init() {
        doChangeSet(
                "changeset-001",
                "tim.schimandle",
                "add index to Education userId and candidateId",
                () -> {
                    var index = new Index()
                            .on("userId", Sort.Direction.ASC).on("candidateId", Sort.Direction.ASC).background();
                    mongoTemplate.indexOps(Education.class).ensureIndex(index);
                    }
                );
        doChangeSet(
                "changeset-002",
                "tim.schimandle",
                "add Education index to candidateId",
                () -> {
                    var index = new Index()
                            .on("candidateId", Sort.Direction.ASC).background();
                    mongoTemplate.indexOps(Education.class).ensureIndex(index);
                }
        );
        doChangeSet(
                "changeset-003",
                "tim.schimandle",
                "add Education index to userId",
                () -> {
                    var index = new Index()
                            .on("userId", Sort.Direction.ASC).background();
                    mongoTemplate.indexOps(Education.class).ensureIndex(index);
                }
        );
        doChangeSet(
                "changeset-004",
                "tim.schimandle",
                "add job index to userId and candidateId",
                () -> {
                    var index = new Index()
                            .on("userId", Sort.Direction.ASC).on("candidateId", Sort.Direction.ASC).background();
                    mongoTemplate.indexOps(Job.class).ensureIndex(index);
                }
        );
        doChangeSet(
                "changeset-005",
                "tim.schimandle",
                "add job index to candidateId",
                () -> {
                    var index = new Index()
                            .on("candidateId", Sort.Direction.ASC).background();
                    mongoTemplate.indexOps(Job.class).ensureIndex(index);
                }
        );
        doChangeSet(
                "changeset-006",
                "tim.schimandle",
                "add job index to userId",
                () -> {
                    var index = new Index()
                            .on("userId", Sort.Direction.ASC).background();
                    mongoTemplate.indexOps(Job.class).ensureIndex(index);
                }
        );
        doChangeSet(
                "changeset-007",
                "tim.schimandle",
                "add candidate index to userId",
                () -> {
                    var index = new Index()
                            .on("userId", Sort.Direction.ASC).background();
                    mongoTemplate.indexOps(Candidate.class).ensureIndex(index);
                }
        );
    }

    private void doChangeSet(String id, String author, String description, Runnable change) {
        log.debug("Starting change set |id: {} |author: {} |description: {}", id, author, description);
        var changeSet = new ChangeSet();
        changeSet.setId(id);
        changeSet.setAuthor(author);
        changeSet.setDescription(description);
        changeSet.setCreatedDate(System.currentTimeMillis());

        var changeSetFromDb = changeSetRepository.findById(id);

        if (
                changeSetFromDb.isEmpty() || //change set doesn't exist
                (!changeSetFromDb.get().isInProgress() && !changeSetFromDb.get().isCompleted()) //change is not complete and not in progress
        ) {
            try {
                changeSetRepository.save(changeSet); //starts work
                //do the work
                log.info("Running change set |id: {} |author: {} |description: {}", id, author, description);
                change.run();

                //update the changeset
                var changeSet1 = changeSetRepository.findById(changeSet.getId());
                changeSet1.ifPresent(c -> {
                    c.setInProgress(false);
                    c.setCompleted(true);
                    changeSetRepository.save(c);
                });
            } catch (Exception e) {
                log.debug("Error with change set |id: {} |author: {} |description: {}", id, author, description);
                //fail the changeset if there's an issue
                changeSet.setInProgress(false);
                changeSet.setCompleted(false);
                changeSet.setDescription(e.getMessage());
                changeSetRepository.save(changeSet);
                //throw the exception again to stop initialization
                throw e;
            }
        } else {
            //skip, already applied
        }
        log.debug("Completed change set |id: {} |author: {} |description: {}", id, author, description);
    }
}
