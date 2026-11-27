package io.github.allyblackcat.feat.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockCookie;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import io.github.allyblackcat.feat.common.exception.GlobalExceptionHandler;

class AuthControllerTest {

    private final SigninSessionService signinSessionService =
        mock(SigninSessionService.class);

    private final MeService meService = mock(MeService.class);

    private final MockMvc mockMvc = MockMvcBuilders
        .standaloneSetup(
            new AuthController(signinSessionService, meService)
        )
        .setControllerAdvice(new GlobalExceptionHandler())
        .build();

    @Test
    @DisplayName("認証成功時にセッショントークンをCookieで返すこと")
    void testSigninWithValidRequest() throws Exception {

        when(signinSessionService.signin(
            "test@example.com", "password123"
        )).thenReturn("test-session-token");

        String requestBody = """
            {
                "email": "test@example.com",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isOk())
            .andExpect(header().exists(HttpHeaders.SET_COOKIE))
            .andExpect(result -> {

                String cookie = result.getResponse()
                    .getHeader(HttpHeaders.SET_COOKIE);

                assertNotNull(cookie);
                assertTrue(cookie.contains(
                    "FEAT_SESSION=test-session-token"
                ));
                assertTrue(cookie.contains("HttpOnly"));
                assertTrue(cookie.contains("Secure"));
                assertTrue(cookie.contains("SameSite=Lax"));
            });

        verify(signinSessionService).signin(
            "test@example.com", "password123"
        );
    }

    @Test
    @DisplayName("emailが空文字の場合、400エラーが返ること")
    void testSigninWithEmptyEmail() throws Exception {

        String requestBody = """
            {
                "email": "",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code")
                .value("ERROR-01002"))
            .andExpect(jsonPath("$.message")
                .value("入力内容に不備があります。"));
    }

    @Test
    @DisplayName("emailが不正な形式の場合、400エラーが返ること")
    void testSigninWithInvalidEmail() throws Exception {

        String requestBody = """
            {
                "email": "invalid-email",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code")
                .value("ERROR-01002"))
            .andExpect(jsonPath("$.message")
                .value("入力内容に不備があります。"));
    }

    @Test
    @DisplayName("emailが254文字を超える場合、400エラーが返ること")
    void testSigninWithTooLongEmail() throws Exception {

        String longEmail =
            "a".repeat(243) + "@example.com";

        String requestBody = String.format("""
            {
                "email": "%s",
                "password": "password123"
            }
            """, longEmail);

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code")
                .value("ERROR-01002"))
            .andExpect(jsonPath("$.message")
                .value("入力内容に不備があります。"));
    }

    @Test
    @DisplayName("passwordが空文字の場合、400エラーが返ること")
    void testSigninWithEmptyPassword() throws Exception {

        String requestBody = """
            {
                "email": "test@example.com",
                "password": ""
            }
            """;

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code")
                .value("ERROR-01002"))
            .andExpect(jsonPath("$.message")
                .value("入力内容に不備があります。"));
    }

    @Test
    @DisplayName("認証失敗時に401エラーが返りCookieを発行しないこと")
    void testSigninAuthenticationFailed() throws Exception {

        when(signinSessionService.signin(
            "test@example.com", "wrong-password"
        )).thenThrow(new AuthenticationFailedException());

        String requestBody = """
            {
                "email": "test@example.com",
                "password": "wrong-password"
            }
            """;

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("ERROR-01001"))
            .andExpect(jsonPath("$.message")
                .value("認証に失敗しました。"))
            .andExpect(header()
                .doesNotExist(HttpHeaders.SET_COOKIE));
    }


    @Test
    @DisplayName("有効なCookieでログインユーザー情報を取得できること")
    void testMeWithValidSession() throws Exception {

        MeResponse response = new MeResponse(
            42L,
            "test@example.com",
            "テストユーザー",
            "テスト",
            false,
            false
        );

        when(meService.getMe("valid-token"))
            .thenReturn(response);

        mockMvc.perform(get("/api/auth/me")
                .cookie(new MockCookie(
                    "FEAT_SESSION", "valid-token"
                )))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(42))
            .andExpect(jsonPath("$.mail_address")
                .value("test@example.com"))
            .andExpect(jsonPath("$.name")
                .value("テストユーザー"))
            .andExpect(jsonPath("$.screen_name")
                .value("テスト"))
            .andExpect(jsonPath("$.administrator")
                .value(false))
            .andExpect(jsonPath("$.must_change_password")
                .value(false))
            .andExpect(jsonPath("$.password_hash")
                .doesNotExist());

        verify(meService).getMe("valid-token");
    }

    @Test
    @DisplayName("Cookieがない場合は401を返すこと")
    void testMeWithoutCookie() throws Exception {

        when(meService.getMe(null))
            .thenThrow(new InvalidSessionException());

        mockMvc.perform(get("/api/auth/me"))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.code")
                .value("ERROR-01009"));

        verify(meService).getMe(null);
    }
}
