package io.github.allyblackcat.feat.auth;

import org.springframework.stereotype.Service;

import io.github.allyblackcat.feat.user.UserRepository;

@Service 
public class AuthService {
    
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}