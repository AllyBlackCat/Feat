package io.github.allyblackcat.feat.auth;

import java.util.Optional;

import io.github.allyblackcat.feat.user.UserRepository;
import io.github.allyblackcat.feat.user.User;

import org.springframework.stereotype.Service;

@Service 
public class AuthService {
    
    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> findUser(String mailAddress) {
        return userRepository.findByMailAddress(mailAddress);
    }

    public void signin(String mailAddress, String password) {
        Optional<User> user = userRepository.findByMailAddress(mailAddress);

        if (user.isEmpty()) {
            throw new AuthenticationFailedException();
        }
    }
}