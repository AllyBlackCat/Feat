package io.github.allyblackcat.feat.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Optional;
import java.time.OffsetDateTime;

import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import org.springframework.security.crypto.password.PasswordEncoder;

import io.github.allyblackcat.feat.user.UserRepository;
import io.github.allyblackcat.feat.user.User;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test 
    @DisplayName("ユーザーが存在しない場合、認証に失敗すること")
    void signinUserNotFound() {
        when(userRepository.findByMailAddress("test@example.com"))
            .thenReturn(Optional.empty());

        assertThrows(
            AuthenticationFailedException.class, 
            () -> authService.signin("test@example.com", "password")
        );
    }

    @Test
    @DisplayName("パスワードが間違っている場合、認証に失敗すること")
    void signinWrongPassword() {
        User user = mock(User.class);

        when(userRepository.findByMailAddress("test@example.com"))
            .thenReturn(Optional.of(user));

        when(user.passwordHash())
            .thenReturn("stored-hash");

        when(passwordEncoder.matches("wrong-password", "stored-hash"))
            .thenReturn(false);

        when(user.available()).thenReturn(true);

        assertThrows(
            AuthenticationFailedException.class,
            () -> authService.signin("test@example.com", "wrong-password")
        );
    }

    @Test
    @DisplayName("パスワードが正しい場合、認証エラーが発生しないこと")
    void signinCorrectPassword() {
        User user = mock(User.class);

        when(userRepository.findByMailAddress("test@example.com"))
            .thenReturn(Optional.of(user));

        when(user.passwordHash())
            .thenReturn("stored-hash");

        when(passwordEncoder.matches("correct-password", "stored-hash"))
            .thenReturn(true);

        when(user.available()).thenReturn(true);

        User result = authService.signin(
            "test@example.com",
            "correct-password"
        );

        assertSame(user, result);
    }

    @Test 
    @DisplayName("利用停止中のユーザーは認証に失敗すること")
    void signinUnavailableUser() {
        User user = mock(User.class);

        when(userRepository.findByMailAddress("test@example.com"))
            .thenReturn(Optional.of(user));
        
        when(user.available()).thenReturn(false);

        assertThrows(
            AuthenticationFailedException.class, 
            () -> authService.signin("test@example.com", "password")
        );

        verifyNoInteractions(passwordEncoder);
    }

    @Test 
    @DisplayName("ロック中のユーザーは認証に失敗すること")
    void signinLockedUser() {
        User user = mock(User.class);

        when(userRepository.findByMailAddress("test@example.com"))
            .thenReturn(Optional.of(user));
        
        when(user.available()).thenReturn(true);

        when(user.lockedAt())
            .thenReturn(OffsetDateTime.now());

        assertThrows(
            AuthenticationFailedException.class,
            () -> authService.signin("test@example.com", "password")
        );

        verifyNoInteractions(passwordEncoder);
    }
}