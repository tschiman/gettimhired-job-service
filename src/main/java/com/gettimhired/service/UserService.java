package com.gettimhired.service;

import com.gettimhired.model.mongo.User;
import com.gettimhired.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User createUser() {
        var password = UUID.randomUUID().toString();
        var user = new User(UUID.randomUUID().toString(), passwordEncoder.encode(password), "email", "password", Collections.emptyList());
        userRepository.save(user);
        return new User(user.id(), password, "email", "password", Collections.emptyList());
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findById(username);
    }

    public void createUser(String email, String password) {
        var user = new User(
                UUID.randomUUID().toString(),
                null,
                email,
                passwordEncoder.encode(password),
                List.of("ROLE_USER")
        );
        userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public String generatePassword(User user) {
        var password = UUID.randomUUID().toString();
        var userToSave = new User(
                user.id(),
                passwordEncoder.encode(password),
                user.email(),
                user.emailPassword(),
                user.roles()
        );

        userRepository.save(userToSave);

        return password;
    }
}
