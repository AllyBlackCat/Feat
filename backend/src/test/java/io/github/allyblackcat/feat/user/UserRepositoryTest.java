package io.github.allyblackcat.feat.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
@SpringBootTest
@ActiveProfiles("test")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired 
    private JdbcTemplate jdbcTemplate;

    @Test 
    @DisplayName("存在しないメールアドレス、ユーザーが取得されないこと")
    void testFindByMailAddressNotFound() {
        var result = userRepository.findByMailAddress(
            "nonexistent@example.invalid"
        );

        assertTrue(result.isEmpty());
    }
    @Test
    @Transactional
    @DisplayName("存在するユーザーを検索できること")
    void testFindByMailAddressFound() {
        // 1. テスト用ユーザーをDBに登録する
        String sql = """
            INSERT INTO users (
                mail_address,
                name,
                screen_name,
                administrator,
                is_available,
                password_hash,
                must_change_password,
                failed_login_count,
                created_at,
                updated_at
            )
            VALUES (
                ?, ?, ?, ?, ?, ?, ?, ?, 
                CURRENT_TIMESTAMP,
                CURRENT_TIMESTAMP
            )
            """;
        

        jdbcTemplate.update(
            sql,
            "repository-test@example.invalid",
            "テストユーザー",
            "テスト",
            false,
            true,
            "dummy-password-hash",
            false,
            0
        );
        // 2. UserRepositoryで検索する
        var result = userRepository.findByMailAddress(
                    "repository-test@example.invalid"
                );


        // 3. 検索結果が存在することを確認する
        assertTrue(result.isPresent());

        // 4. 取得したユーザー情報を検証する
        User user = result.orElseThrow();

        assertEquals(
            "repository-test@example.invalid",
            user.mailAddress()
        );

        assertEquals(
            "テストユーザー",
            user.name()
        );

        assertEquals(
            "テスト",
            user.screenName()
        );

    }
}
