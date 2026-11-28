package io.github.allyblackcat.feat.auth;

import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SessionValidationServiceTest {

    private final SessionTokenService tokenService =
        mock(SessionTokenService.class);

    private final SessionRepository sessionRepository =
        mock(SessionRepository.class);

    private final SessionValidationService service =
        new SessionValidationService(
            tokenService,
            sessionRepository
        );

    @Test
    @DisplayName("有効なセッションを検証できること")
    void validSession() {

        OffsetDateTime now = OffsetDateTime.now();

        Session session = new Session(
            1L, 42L, "hashed-token",
            now.minusHours(1),
            now.minusMinutes(5),
            now.plusHours(7),
            null
        );

        when(tokenService.hashToken("raw-token"))
            .thenReturn("hashed-token");

        when(sessionRepository.findByTokenHash("hashed-token"))
            .thenReturn(Optional.of(session));

        when(sessionRepository.updateLastActivity(
            eq("hashed-token"),
            any(OffsetDateTime.class)
        )).thenReturn(1);

        Session result = service.validate("raw-token");

        assertEquals(42L, result.userId());

        verify(sessionRepository).updateLastActivity(
            eq("hashed-token"),
            any(OffsetDateTime.class)
        );
    }

    @Test
    @DisplayName("存在しないセッションは認証に失敗すること")
    void sessionNotFound() {

        when(tokenService.hashToken("unknown-token"))
            .thenReturn("unknown-hash");

        when(sessionRepository.findByTokenHash("unknown-hash"))
            .thenReturn(Optional.empty());

        assertThrows(
            InvalidSessionException.class,
            () -> service.validate("unknown-token")
        );

        verify(sessionRepository, never())
            .updateLastActivity(anyString(), any());
    }

    @Test
    @DisplayName("30分以上操作がないセッションは無効になること")
    void idleSessionExpired() {

        OffsetDateTime now = OffsetDateTime.now();

        Session session = new Session(
            1L, 42L, "hashed-token",
            now.minusHours(1),
            now.minusMinutes(31),
            now.plusHours(7),
            null
        );

        when(tokenService.hashToken("raw-token"))
            .thenReturn("hashed-token");

        when(sessionRepository.findByTokenHash("hashed-token"))
            .thenReturn(Optional.of(session));

        assertThrows(
            InvalidSessionException.class,
            () -> service.validate("raw-token")
        );

        verify(sessionRepository, never())
            .updateLastActivity(anyString(), any());
    }

    @Test
    @DisplayName("失効済みセッションは認証に失敗すること")
    void revokedSession() {

        OffsetDateTime now = OffsetDateTime.now();

        Session session = new Session(
            1L, 42L, "hashed-token",
            now.minusHours(1),
            now.minusMinutes(5),
            now.plusHours(7),
            now.minusMinutes(1)
        );

        when(tokenService.hashToken("raw-token"))
            .thenReturn("hashed-token");

        when(sessionRepository.findByTokenHash("hashed-token"))
            .thenReturn(Optional.of(session));

        assertThrows(
            InvalidSessionException.class,
            () -> service.validate("raw-token")
        );
    }

    @Test
    @DisplayName("8時間の有効期限を過ぎたセッションは無効になること")
    void absoluteSessionExpired() {

        OffsetDateTime now = OffsetDateTime.now();

        Session session = new Session(
            1L, 42L, "hashed-token",
            now.minusHours(9),
            now.minusMinutes(5),
            now.minusHours(1),
            null
        );

        when(tokenService.hashToken("raw-token"))
            .thenReturn("hashed-token");

        when(sessionRepository.findByTokenHash("hashed-token"))
            .thenReturn(Optional.of(session));

        assertThrows(
            InvalidSessionException.class,
            () -> service.validate("raw-token")
        );
    }
}
