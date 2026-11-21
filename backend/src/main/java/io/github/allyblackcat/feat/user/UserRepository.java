package io.github.allyblackcat.feat.user;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;


import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@Repository 
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findByMailAddress(String mailAddress) {
        
        String sql = """
            SELECT *
            FROM users
            WHERE mail_address = ?
            """;

        RowMapper<User> rowMapper = (rs, rowNum) -> {
            return new User(
                rs.getLong("id"),
                rs.getString("mail_address"),
                rs.getString("name"),
                rs.getString("screen_name"),
                rs.getString("department"),
                rs.getBoolean("administrator"),
                rs.getBoolean("is_available"),
                rs.getString("password_hash"),
                rs.getBoolean("must_change_password"),
                rs.getInt("failed_login_count"),
                rs.getObject("last_failed_login_at",
                        OffsetDateTime.class),
                rs.getObject("locked_at",
                        OffsetDateTime.class),
                rs.getObject("created_at",
                        OffsetDateTime.class),
                rs.getObject("updated_at",
                        OffsetDateTime.class)
            );
        };
        
        List<User> users = jdbcTemplate.query(
            sql,
            rowMapper,
            mailAddress
        );

        return users.stream().findFirst();
    }

}