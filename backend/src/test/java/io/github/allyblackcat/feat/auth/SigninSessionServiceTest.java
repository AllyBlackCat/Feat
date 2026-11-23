package io.github.allyblackcat.feat.auth;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

import io.github.allyblackcat.feat.user.User;

class SigninSessionServiceTest {

    private final AuthService authService = mock(AuthService.class);
    private final SessionTokenService tokenService = mock(SessionTokenService.class);
    private final SessionRepository sessionRepository = mock(SessionRepository.class);
    private final SigninSessionService service = 
        new SigninSessionService(
            authService,
            tokenService,
            sessionRepository
        );
    
    @Test 
    @DisplayName("認証成功時にDBセッションを登録しトークンを返す")
    void signinCreatesSession() {

        User user = mock(User.class);

        when(user.id()).thenReturn(42L);

        when(authService.signin(
            "test@example.com", "password123"
        )).thenReturn(user);

        when(tokenService.generateToken())
            .thenReturn("test-token");
        
        when(tokenService.hashToken("test-token"))
            .thenReturn("test-token-hash");
        
        OffsetDateTime before = OffsetDateTime.now();

        String result = service.signin(
            "test@example.com",
            "password123"
        );

        OffsetDateTime after = OffsetDateTime.now();

        assertEquals("test-token", result);

        verify(sessionRepository).create(
            eq(42L), 
            eq("test-token-hash"), 
            argThat(expiresAt -> !expiresAt.isBefore(before.plusHours(8))
                    && !expiresAt.isAfter(after.plusHours(8))
            
            )
        );
    }


}