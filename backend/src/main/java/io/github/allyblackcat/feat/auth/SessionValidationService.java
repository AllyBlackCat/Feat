package io.github.allyblackcat.feat.auth;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class SessionValidationService {

    private final SessionTokenService tokenService;
    private final SessionRepository sessionRepository;

    public SessionValidationService(
        SessionTokenService tokenService,
        SessionRepository sessionRepository) {
        
        this.tokenService = tokenService;
        this.sessionRepository = sessionRepository;
    }

    @Transactional 
    public Session validate(String token) {

        if (token == null || token.isBlank()) {
            throw new InvalidSessionException();
        }

        String tokenHash = tokenService.hashToken(token);

        Session session = sessionRepository
                .findByTokenHash(tokenHash)
                .orElseThrow(InvalidSessionException::new);

        OffsetDateTime now = OffsetDateTime.now();

        if (session.revokedAt() != null 
                || !session.expiresAt().isAfter(now)
                || !session.lastActivityAt().isAfter(now.minusMinutes(30))) {
                    throw new InvalidSessionException();
                }
        
        int updatedRows = sessionRepository.updateLastActivity(tokenHash, now);

        if (updatedRows != 1) {
            throw new InvalidSessionException();
        }

        return session;
        
    }
}