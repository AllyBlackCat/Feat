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
import org.springframework.mock.web.MockCookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import jakarta.servlet.http.Cookie;
import com.jayway.jsonpath.JsonPath;

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

    @Test
    @Transactional
    @DisplayName("ログイン後のCookieでDBから本人情報を取得できること")
    void signinThenGetMeSuccessfully() throws Exception {

        String email = "me-integration@example.invalid";
        String password = "test-password-123";

        // テストユーザーをDBに登録
        Long userId = jdbcTemplate.queryForObject("""
            INSERT INTO users (
                mail_address,
                name,
                screen_name,
                password_hash,
                must_change_password,
                administrator
            )
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id
            """,
            Long.class,
            email,
            "結合テストユーザー",
            "結合テスト",
            passwordEncoder.encode(password),
            false,
            false
        );

        // ログインしてCookieを取得
        String requestBody = """
            {
                "email": "me-integration@example.invalid",
                "password": "test-password-123"
            }
            """;

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

        assertNotNull(setCookie);

        String token = setCookie
            .split(";", 2)[0]
            .substring("FEAT_SESSION=".length());

        // DBのセッション登録を確認
        String tokenHash = tokenService.hashToken(token);

        Session session = sessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow();

        assertEquals(userId, session.userId());

        OffsetDateTime before = OffsetDateTime.now();

        // 発行されたCookieを使って本人情報を取得
        mockMvc.perform(
                get("/api/auth/me")
                    .cookie(new MockCookie(
                        "FEAT_SESSION", token
                    ))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id")
                .value(userId))
            .andExpect(jsonPath("$.mail_address")
                .value(email))
            .andExpect(jsonPath("$.name")
                .value("結合テストユーザー"))
            .andExpect(jsonPath("$.screen_name")
                .value("結合テスト"))
            .andExpect(jsonPath("$.administrator")
                .value(false))
            .andExpect(jsonPath("$.must_change_password")
                .value(false))
            .andExpect(jsonPath("$.password_hash")
                .doesNotExist());

        // 最終操作日時の更新を確認
        Session updatedSession = sessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow();

        assertFalse(
            updatedSession.lastActivityAt().isBefore(before)
        );
    }

    @Test
    @DisplayName("存在しないセッションでは401を返すこと")
    void getMeWithInvalidSession() throws Exception {

        mockMvc.perform(
                get("/api/auth/me")
                    .cookie(new MockCookie(
                        "FEAT_SESSION",
                        "nonexistent-session-token"
                    ))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("ERROR-01009"))
            .andExpect(jsonPath("$.message")
                .value("認証に失敗しました。"))
            .andExpect(jsonPath("$.id").doesNotExist());
    }

    @Test
    @Transactional
    @DisplayName("利用停止ユーザーは有効なセッションでも401を返すこと")
    void getMeWithUnavailableUser() throws Exception {

        String email = "unavailable-me@example.invalid";
        String password = "test-password-123";

        // 認証可能なユーザーを登録
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
            "利用停止テストユーザー",
            "利用停止テスト",
            passwordEncoder.encode(password),
            false
        );

        // 正常にログインしてトークンを取得
        String requestBody = """
            {
                "email": "unavailable-me@example.invalid",
                "password": "test-password-123"
            }
            """;

        String setCookie = mockMvc.perform(
                post("/api/auth/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getHeader(HttpHeaders.SET_COOKIE);

        assertNotNull(setCookie);

        String token = setCookie
            .split(";", 2)[0]
            .substring("FEAT_SESSION=".length());

        String tokenHash = tokenService.hashToken(token);

        // セッションが実際に存在することを確認
        Session session = sessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow();

        assertEquals(userId, session.userId());

        // ログイン後にユーザーを利用停止
        int updatedRows = jdbcTemplate.update("""
            UPDATE users
            SET is_available = FALSE
            WHERE id = ?
            """,
            userId
        );

        assertEquals(1, updatedRows);

        // セッションが残っていても本人情報は取得できない
        mockMvc.perform(
                get("/api/auth/me")
                    .cookie(new MockCookie(
                        "FEAT_SESSION", token
                    ))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("ERROR-01009"))
            .andExpect(jsonPath("$.id").doesNotExist())
            .andExpect(jsonPath("$.mail_address").doesNotExist());

    }

    @Test
    @DisplayName("CSRFトークンを取得できること")
    void getCsrfTokenSuccessfully() throws Exception {

        mockMvc.perform(get("/api/auth/csrf"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").isNotEmpty())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    @Transactional
    @DisplayName("CSRF検証後にログアウトしセッションを失効できること")
    void signoutRevokesSession() throws Exception {

        String email = "signout-test@example.invalid";
        String password = "test-password-123";

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
            "ログアウトテスト",
            "ログアウト",
            passwordEncoder.encode(password),
            false
        );

        assertNotNull(userId);

        // ログイン
        String signinCookie = mockMvc.perform(
                post("/api/auth/signin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {
                            "email": "signout-test@example.invalid",
                            "password": "test-password-123"
                        }
                        """)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getHeader(HttpHeaders.SET_COOKIE);

        assertNotNull(signinCookie);

        String token = signinCookie
            .split(";", 2)[0]
            .substring("FEAT_SESSION=".length());

        String tokenHash = tokenService.hashToken(token);

        // CSRFトークンを取得
        var csrfResponse = mockMvc.perform(
                get("/api/auth/csrf")
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        String csrfToken = JsonPath.read(
            csrfResponse.getContentAsString(),
            "$.token"
        );

        Cookie csrfCookie = csrfResponse.getCookie(
            "__Host-XSRF-TOKEN"
        );

        assertNotNull(csrfCookie);

        // ログアウト
        String deletedCookie = mockMvc.perform(
                post("/api/auth/signout")
                    .cookie(
                        new MockCookie("FEAT_SESSION", token),
                        csrfCookie
                    )
                    .header("X-XSRF-TOKEN", csrfToken)
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getHeader(HttpHeaders.SET_COOKIE);

        assertNotNull(deletedCookie);
        assertTrue(deletedCookie.contains("FEAT_SESSION="));
        assertTrue(deletedCookie.contains("Max-Age=0"));

        // DBセッションが失効していることを確認
        Session session = sessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow();

        assertNotNull(session.revokedAt());

        // 同じCookieでは本人情報を取得できない
        mockMvc.perform(
                get("/api/auth/me")
                    .cookie(new MockCookie(
                        "FEAT_SESSION", token
                    ))
            )
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("ERROR-01009"));
    }

    @Test
    @DisplayName("CSRFトークンなしのログアウトは403を返すこと")
    void signoutWithoutCsrfReturnsForbidden() throws Exception {

        mockMvc.perform(
                post("/api/auth/signout")
            )
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code")
                .value("ERROR-00001"));
    }
    private record CsrfCredentials(
        String token,
        Cookie cookie
    ) {}

    private CsrfCredentials issueCsrf() throws Exception {

        var response = mockMvc.perform(get("/api/auth/csrf"))
            .andExpect(status().isOk())
            .andReturn()
            .getResponse();

        String csrfToken = JsonPath.read(
            response.getContentAsString(), "$.token"
        );

        Cookie csrfCookie =
            response.getCookie("__Host-XSRF-TOKEN");

        assertNotNull(csrfCookie);

        return new CsrfCredentials(csrfToken, csrfCookie);
    }

    private Long createSignoutTestUser(String email) {

        return jdbcTemplate.queryForObject("""
            INSERT INTO users (
                mail_address, name, screen_name,
                password_hash, must_change_password
            )
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
            """,
            Long.class,
            email,
            "ログアウト検証",
            "検証",
            "dummy-hash",
            false
        );
    }

    private String createSignoutTestSession(Long userId) {

        String token = tokenService.generateToken();

        sessionRepository.create(
            userId,
            tokenService.hashToken(token),
            OffsetDateTime.now().plusHours(8)
        );

        return token;
    }
    @Test
    @Transactional
    @DisplayName("二重ログアウトでも200を返すこと")
    void signoutTwiceReturnsOk() throws Exception {

        Long userId = createSignoutTestUser(
            "signout-twice@example.invalid"
        );

        String token = createSignoutTestSession(userId);
        String tokenHash = tokenService.hashToken(token);

        CsrfCredentials csrf = issueCsrf();

        // 1回目
        mockMvc.perform(post("/api/auth/signout")
                .cookie(
                    new MockCookie("FEAT_SESSION", token),
                    csrf.cookie()
                )
                .header("X-XSRF-TOKEN", csrf.token()))
            .andExpect(status().isOk());

        OffsetDateTime firstRevokedAt = sessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow()
            .revokedAt();

        assertNotNull(firstRevokedAt);

        // 2回目
        mockMvc.perform(post("/api/auth/signout")
                .cookie(
                    new MockCookie("FEAT_SESSION", token),
                    csrf.cookie()
                )
                .header("X-XSRF-TOKEN", csrf.token()))
            .andExpect(status().isOk());

        OffsetDateTime secondRevokedAt = sessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow()
            .revokedAt();

        assertEquals(
            firstRevokedAt.toInstant(),
            secondRevokedAt.toInstant()
        );
    }

    @Test
    @DisplayName("セッションがなくてもログアウトできること")
    void signoutWithoutSessionReturnsOk() throws Exception {

        CsrfCredentials csrf = issueCsrf();

        String setCookie = mockMvc.perform(
                post("/api/auth/signout")
                    .cookie(csrf.cookie())
                    .header("X-XSRF-TOKEN", csrf.token())
            )
            .andExpect(status().isOk())
            .andReturn()
            .getResponse()
            .getHeader(HttpHeaders.SET_COOKIE);

        assertNotNull(setCookie);
        assertTrue(setCookie.contains("FEAT_SESSION="));
        assertTrue(setCookie.contains("Max-Age=0"));
    }

    @Test
    @Transactional
    @DisplayName("ログアウトしても別端末のセッションは維持すること")
    void signoutPreservesOtherSession() throws Exception {

        Long userId = createSignoutTestUser(
            "signout-multiple@example.invalid"
        );

        // 同じユーザーで別々のセッションを発行
        String tokenA = createSignoutTestSession(userId);
        String tokenB = createSignoutTestSession(userId);

        assertNotEquals(tokenA, tokenB);

        CsrfCredentials csrf = issueCsrf();

        // 端末Aだけログアウト
        mockMvc.perform(post("/api/auth/signout")
                .cookie(
                    new MockCookie("FEAT_SESSION", tokenA),
                    csrf.cookie()
                )
                .header("X-XSRF-TOKEN", csrf.token()))
            .andExpect(status().isOk());

        Session sessionA = sessionRepository
            .findByTokenHash(tokenService.hashToken(tokenA))
            .orElseThrow();

        Session sessionB = sessionRepository
            .findByTokenHash(tokenService.hashToken(tokenB))
            .orElseThrow();

        assertNotNull(sessionA.revokedAt());
        assertNull(sessionB.revokedAt());

        // 端末Aはアクセス不可
        mockMvc.perform(get("/api/auth/me")
                .cookie(new MockCookie(
                    "FEAT_SESSION", tokenA
                )))
            .andExpect(status().isUnauthorized());

        // 端末Bは引き続きアクセス可能
        mockMvc.perform(get("/api/auth/me")
                .cookie(new MockCookie(
                    "FEAT_SESSION", tokenB
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId));
    }

    @Test
    @Transactional
    @DisplayName("不正なCSRFトークンではセッションを失効しないこと")
    void signoutWithInvalidCsrfReturnsForbidden() throws Exception {

        Long userId = createSignoutTestUser(
            "signout-invalid-csrf@example.invalid"
        );

        String token = createSignoutTestSession(userId);
        String tokenHash = tokenService.hashToken(token);

        CsrfCredentials csrf = issueCsrf();

        mockMvc.perform(post("/api/auth/signout")
                .cookie(
                    new MockCookie("FEAT_SESSION", token),
                    csrf.cookie()
                )
                .header("X-XSRF-TOKEN", "invalid-csrf-token"))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.code")
                .value("ERROR-00001"));

        Session session = sessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow();

        assertNull(session.revokedAt());

        // CSRF検証失敗後も元のセッションは有効
        mockMvc.perform(get("/api/auth/me")
                .cookie(new MockCookie(
                    "FEAT_SESSION", token
                )))
            .andExpect(status().isOk());
    }
}
