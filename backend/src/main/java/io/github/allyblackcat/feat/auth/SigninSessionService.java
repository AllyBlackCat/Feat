package io.github.allyblackcat.feat.auth;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.allyblackcat.feat.user.User;

@Service 
public class SigninSessionService {

    private final AuthService authService;
    private final SessionTokenService tokenService;
    private final SessionRepository sessionRepository;

    public SigninSessionService(
        AuthService authService,
        SessionTokenService tokenService,
        SessionRepository sessionRepository) {

        this.authService = authService;
        this.tokenService = tokenService;
        this.sessionRepository = sessionRepository;
    }

    @Transactional 
    public String signin(String email, String password) {

        User user = authService.signin(email, password);

        String token = tokenService.generateToken();
        String tokenHash = tokenService.hashToken(token);

        OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(8);

        sessionRepository.create(user.id(), tokenHash, expiresAt);

        return token;
    }
}