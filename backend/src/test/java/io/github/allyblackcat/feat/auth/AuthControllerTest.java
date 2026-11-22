package io.github.allyblackcat.feat.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.allyblackcat.feat.common.exception.GlobalExceptionHandler;
import io.github.allyblackcat.feat.user.User;



public class AuthControllerTest {

    private final AuthService authService = mock(AuthService.class);

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AuthController(authService))
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    // 正常系
    @Test
    @DisplayName("emailとpasswordが有効な場合、入力検証を通過すること")
    void testSigninWithValidRequest() throws Exception {

        User user = mock(User.class);

        when(authService.signin("test@example.com", "password123"))
            .thenReturn(user);

        when(user.id()).thenReturn(1L);


        String requestBody = """ 
            {
                "email": "test@example.com",
                "password": "password123"
            }
        """;

        MockHttpSession session = new MockHttpSession();
        String oldSessionId = session.getId();
        mockMvc.perform(post("/api/auth/signin")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(request().sessionAttribute("USER_ID", 1L))
                .andExpect(result ->
                    assertNotEquals(
                        oldSessionId,
                        result.getRequest().getSession(false).getId()
                    )
                );

        verify(authService).signin("test@example.com", "password123");
    }

    // 異常系
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
                .andExpect(jsonPath("$.code").value("ERROR-01002"))
                .andExpect(jsonPath("$.message").value("入力内容に不備があります。"));
    }

    @Test 
    @DisplayName ("emailが不正な形式の場合、400エラーが返ること")
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
                .andExpect(jsonPath("$.code").value("ERROR-01002"))
                .andExpect(jsonPath("$.message").value("入力内容に不備があります。"));
    }

    @Test 
    @DisplayName ("emailが254文字を超える場合、400エラーが返ること")
    void testSigninWithTooLongEmail() throws Exception {
        String longEmail = "a".repeat(243) + "@example.com";
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
                .andExpect(jsonPath("$.code").value("ERROR-01002"))
                .andExpect(jsonPath("$.message").value("入力内容に不備があります。"));
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
                .andExpect(jsonPath("$.code").value("ERROR-01002"))
                .andExpect(jsonPath("$.message").value("入力内容に不備があります。"));
    }

    @Test 
    @DisplayName("認証失敗時に401エラーが返ること")
    void testSigninAuthenticationFailed() throws Exception {
        when(authService.signin("test@example.com", "wrong-password"))
                .thenThrow(new AuthenticationFailedException());

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
                .andExpect(jsonPath("$.code").value("ERROR-01001"))
                .andExpect(jsonPath("$.message").value("認証に失敗しました。"))
                .andExpect(result ->
                    assertNull(result.getRequest().getSession(false))
                );
    }
}