package io.github.allyblackcat.feat.auth;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
public class SessionRepositoryTest {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    @DisplayName("セッションをDBに登録できること")
    void createSessionSuccessfully() {

        // テスト用ユーザーを登録
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
            "session-test@example.invalid",
            "テストユーザー",
            "テスト",
            "dummy-hash",
            false
        );

        String tokenHash = "a".repeat(64);
        OffsetDateTime expiresAt =
                OffsetDateTime.now()
                    .plusHours(8)
                    .truncatedTo(java.time.temporal.ChronoUnit.MICROS);

        // テスト対象のメソッド
        sessionRepository.create(
            userId,
            tokenHash,
            expiresAt
        );

        // DBから登録結果を取得
        Long actualUserId = jdbcTemplate.queryForObject("""
            SELECT user_id
            FROM sessions
            WHERE token_hash = ?
            """,
            Long.class,
            tokenHash
        );

        OffsetDateTime actualExpiresAt =
                jdbcTemplate.queryForObject("""
                    SELECT expires_at
                    FROM sessions
                    WHERE token_hash = ?
                    """,
                    (rs, rowNum) -> rs.getObject(
                        "expires_at",
                        OffsetDateTime.class
                    ),
                    tokenHash
                );

        assertEquals(userId, actualUserId);
        assertEquals(
            expiresAt.toInstant(),
            actualExpiresAt.toInstant()
        );
    }
    @Test
    @Transactional
    @DisplayName("存在するセッションを検索できること")
    void findByTokenHashFound() {

        // テスト用ユーザーを登録
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
            "session-find@example.invalid",
            "テストユーザー",
            "テスト",
            "dummy-hash",
            false
        );

        String tokenHash = "b".repeat(64);
        OffsetDateTime expiresAt = OffsetDateTime.now()
            .plusHours(8)
            .truncatedTo(java.time.temporal.ChronoUnit.MICROS);

        // セッション登録
        sessionRepository.create(userId, tokenHash, expiresAt);

        // セッション検索
        Session session = sessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow();

        // 登録内容の検証
        assertEquals(userId, session.userId());
        assertEquals(tokenHash, session.tokenHash());
        assertEquals(
            expiresAt.toInstant(),
            session.expiresAt().toInstant()
        );
    }
    @Test
    @DisplayName("存在しないセッションは取得されないこと")
    void findByTokenHashNotFound() {

        var result = sessionRepository.findByTokenHash(
            "nonexistent-token-hash"
        );

        assertTrue(result.isEmpty());
    }

    @Test
    @Transactional
    @DisplayName("セッションの最終操作日時を更新できること")
    void updateLastActivitySuccessfully() {

        Long userId = jdbcTemplate.queryForObject("""
            INSERT INTO users (
                mail_address, name, screen_name,
                password_hash, must_change_password
            )
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
            """,
            Long.class,
            "session-update@example.invalid",
            "テストユーザー",
            "テスト",
            "dummy-hash",
            false
        );

        String tokenHash = "c".repeat(64);

        OffsetDateTime expiresAt = OffsetDateTime.now()
            .plusHours(8)
            .truncatedTo(java.time.temporal.ChronoUnit.MICROS);

        sessionRepository.create(userId, tokenHash, expiresAt);

        OffsetDateTime updatedAt = OffsetDateTime.now()
            .plusMinutes(5)
            .truncatedTo(java.time.temporal.ChronoUnit.MICROS);

        int updatedRows =
            sessionRepository.updateLastActivity(tokenHash, updatedAt);

        Session session = sessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow();

        assertEquals(1, updatedRows);
        assertEquals(
            updatedAt.toInstant(),
            session.lastActivityAt().toInstant()
        );
    }

    @Test
    @Transactional
    @DisplayName("セッションを失効できること")
    void revokeSessionSuccessfully() {

        Long userId = jdbcTemplate.queryForObject("""
            INSERT INTO users (
                mail_address, name, screen_name,
                password_hash, must_change_password
            )
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
            """,
            Long.class,
            "session-revoke@example.invalid",
            "テストユーザー",
            "テスト",
            "dummy-hash",
            false
        );

        String tokenHash = "d".repeat(64);

        OffsetDateTime expiresAt = OffsetDateTime.now()
            .plusHours(8)
            .truncatedTo(java.time.temporal.ChronoUnit.MICROS);

        sessionRepository.create(userId, tokenHash, expiresAt);

        OffsetDateTime revokedAt = OffsetDateTime.now()
            .truncatedTo(java.time.temporal.ChronoUnit.MICROS);

        int updatedRows =
            sessionRepository.revoke(tokenHash, revokedAt);

        Session session = sessionRepository
            .findByTokenHash(tokenHash)
            .orElseThrow();

        assertEquals(1, updatedRows);
        assertEquals(
            revokedAt.toInstant(),
            session.revokedAt().toInstant()
        );

        // 二重失効は更新しない
        assertEquals(
            0,
            sessionRepository.revoke(tokenHash, revokedAt.plusSeconds(1))
        );
    }
}
