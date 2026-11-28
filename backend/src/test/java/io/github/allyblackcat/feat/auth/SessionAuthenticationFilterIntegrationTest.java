package io.github.allyblackcat.feat.auth;

import java.time.OffsetDateTime;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockCookie;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(SessionAuthenticationFilterIntegrationTest.TestController.class)
class SessionAuthenticationFilterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private SessionTokenService tokenService;

    @Autowired
    private MeService meService;

    @RestController
    static class TestController {

        @GetMapping("/api/test/authentication")
        public Map<String, Object> authentication(
                Authentication authentication) {

            MeResponse user =
                (MeResponse) authentication.getPrincipal();

            return Map.of(
                "id", user.id(),
                "role", authentication.getAuthorities()
                    .iterator()
                    .next()
                    .getAuthority(),
                "authenticated",
                authentication.isAuthenticated()
            );
        }
    }

    private Long createUser(
            String email,
            boolean administrator) {

        return jdbcTemplate.queryForObject("""
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
            "認証テスト",
            "認証",
            "dummy-hash",
            false,
            administrator
        );
    }

    private String createSession(Long userId) {

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
    @DisplayName("一般ユーザーが認証されROLE_USERを取得すること")
    void validSessionAllowsAccess() throws Exception {

        Long userId = createUser(
            "filter-user@example.invalid",
            false
        );

        String token = createSession(userId);

        MeResponse me = meService.getMe(token);

        assertEquals(userId, me.id());
        assertFalse(me.administrator());

        mockMvc.perform(
                get("/api/test/authentication")
                    .cookie(new MockCookie(
                        "FEAT_SESSION", token
                    ))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.role")
                .value("ROLE_USER"))
            .andExpect(jsonPath("$.authenticated")
                .value(true));
    }

    @Test
    @Transactional
    @DisplayName("システム管理者が認証されROLE_ADMINを取得すること")
    void administratorSessionAllowsAccess() throws Exception {

        Long userId = createUser(
            "filter-admin@example.invalid",
            true
        );

        String token = createSession(userId);

        MeResponse me = meService.getMe(token);

        assertEquals(userId, me.id());
        assertTrue(me.administrator());

        mockMvc.perform(
                get("/api/test/authentication")
                    .cookie(new MockCookie(
                        "FEAT_SESSION", token
                    ))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(userId))
            .andExpect(jsonPath("$.role")
                .value("ROLE_ADMIN"))
            .andExpect(jsonPath("$.authenticated")
                .value(true));
    }
}
