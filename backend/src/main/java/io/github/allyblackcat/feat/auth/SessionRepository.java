package io.github.allyblackcat.feat.auth;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.Optional;

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

    public Optional<Session> findByTokenHash(String tokenHash) {

        String sql = """
            SELECT
                id,
                user_id,
                token_hash,
                created_at,
                last_activity_at,
                expires_at,
                revoked_at
            FROM sessions
            WHERE token_hash = ?
            """;

        return jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new Session(
                rs.getLong("id"),
                rs.getLong("user_id"),
                rs.getString("token_hash"),
                rs.getObject("created_at", OffsetDateTime.class),
                rs.getObject("last_activity_at", OffsetDateTime.class),
                rs.getObject("expires_at", OffsetDateTime.class),
                rs.getObject("revoked_at", OffsetDateTime.class)
            ),
            tokenHash
        ).stream().findFirst();
    }
}