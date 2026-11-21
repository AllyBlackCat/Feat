package io.github.allyblackcat.feat.auth;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import io.github.allyblackcat.feat.common.exception.GlobalExceptionHandler;
public class AuthControllerTest {

    private final MockMvc mockMvc = MockMvcBuilders
            .standaloneSetup(new AuthController())
            .setControllerAdvice(new GlobalExceptionHandler())
            .build();

    // 正常系
    @Test
    @DisplayName("emailとpasswordが有効な場合、入力検証を通過すること")
    void testSigninWithValidRequest() throws Exception {
        String requestBody = """ 
            {
                "email": "test@example.com",
                "password": "password123"
            }
        """;
        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
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
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
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
                .andExpect(status().isBadRequest());
    }
}