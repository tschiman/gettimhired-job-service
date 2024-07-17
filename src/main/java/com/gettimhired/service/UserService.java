package com.gettimhired.service;

import com.gettimhired.model.dto.UserDTO;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    public Optional<UserDTO> findUserByUsername(String bark) {
        return Optional.empty();
    }
}
