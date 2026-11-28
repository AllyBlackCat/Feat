package io.github.allyblackcat.feat.auth;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class SignoutSessionService {

    private final SessionTokenService tokenService;
    private final SessionRepository sessionRepository;

    public SignoutSessionService(
        SessionTokenService tokenService,
        SessionRepository sessionRepository
    ) {
        this.tokenService = tokenService;
        this.sessionRepository = sessionRepository;
    }

    @Transactional 
    public void signout(String token) {

        if (token == null || token.isBlank()) {
            return;
        }

        String tokenHash = tokenService.hashToken(token);

        sessionRepository.revoke(tokenHash, OffsetDateTime.now());
    }
}