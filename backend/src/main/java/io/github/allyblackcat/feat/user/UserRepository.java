package io.github.allyblackcat.feat.user;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

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
        
        return Optional.empty();
    }

}