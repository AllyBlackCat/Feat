package io.github.allyblackcat.feat.auth;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionTokenService tokenService;

    @Test
    @Transactional
    @DisplayName("ログイン成功時にDBセッションを登録してCookieを返すこと")
    void signinCreatesDatabaseSessionAndCookie() throws Exception {

        String email = "integration-test@example.invalid";
        String password = "test-password-123";

        // 1. 実際に認証可能なユーザーをDBへ登録
        Long userId = jdbcTemplate.queryForObject("""
            INSERT INTO users (
                mail_address,
                name,
                screen_name,
                password_hash,
                must_change_password
            )
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
            """,
            Long.class,
            email,
            "結合テストユーザー",
            "結合テスト",
            passwordEncoder.encode(password),
            false
        );

        // 2. HTTPリクエストでログイン
        String requestBody = """
            {
                "email": "integration-test@example.invalid",
                "password": "test-password-123"
            }
            """;

        OffsetDateTime before = OffsetDateTime.now();

        String setCookie = mockMvc.perform(
                post("/api/auth/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andReturn()
            .getResponse()
            .getHeader(HttpHeaders.SET_COOKIE);

        OffsetDateTime after = OffsetDateTime.now();

        // 3. Cookieからトークンを取得
        assertNotNull(setCookie);
        assertTrue(setCookie.startsWith("FEAT_SESSION="));
        assertTrue(setCookie.contains("HttpOnly"));
        assertTrue(setCookie.contains("Secure"));

        String token = setCookie
            .split(";", 2)[0]
            .substring("FEAT_SESSION=".length());

        assertEquals(64, token.length());

        // 4. Cookieのトークンをハッシュ化してDB検索
        String tokenHash = tokenService.hashToken(token);

        Session session = sessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow();

        // 5. DB登録内容を検証
        assertEquals(userId, session.userId());
        assertEquals(tokenHash, session.tokenHash());
        assertNotEquals(token, session.tokenHash());
        assertNull(session.revokedAt());

        // 有効期限がログインから8時間後であること
        assertFalse(
            session.expiresAt().isBefore(before.plusHours(8))
        );
        assertFalse(
            session.expiresAt().isAfter(after.plusHours(8))
        );
    }
}
