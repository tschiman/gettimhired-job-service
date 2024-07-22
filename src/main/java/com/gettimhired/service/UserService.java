package com.gettimhired.service;

import com.gettimhired.model.dto.UserDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Optional;

@Service
public class UserService {

    Logger log = LoggerFactory.getLogger(UserService.class);
    private final RestClient userServiceRestClient;

    public UserService(RestClient userServiceRestClient) {
        this.userServiceRestClient = userServiceRestClient;
    }

    public Optional<UserDTO> findUserById(String id) {
         var result = userServiceRestClient
                .get()
                .uri("/api/users/" + id + "/id")
                .retrieve()
                .toEntity(UserDTO.class);
         if (result.getStatusCode().is2xxSuccessful()) {
             return Optional.ofNullable(result.getBody());
         } else {
             log.error("GET findUserById id={} httpStatus={}", id, result.getStatusCode());
             return Optional.empty();
         }
    }
}
