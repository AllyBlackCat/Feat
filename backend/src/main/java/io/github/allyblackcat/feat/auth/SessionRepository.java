package io.github.allyblackcat.feat.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SessionRepository {

    private final JdbcTemplate jdbcTemplate;

    public SessionRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void create(
            Long userId,
            String tokenHash,
            java.time.OffsetDateTime expiresAt) {

        String sql = """
            INSERT INTO sessions (
                user_id,
                token_hash,
                expires_at
            )
            VALUES (?, ?, ?)
            """;

        jdbcTemplate.update(
            sql,
            userId,
            tokenHash,
            expiresAt
        );
    }
}