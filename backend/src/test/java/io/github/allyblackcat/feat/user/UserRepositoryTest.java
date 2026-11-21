package io.github.allyblackcat.feat.user;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("local")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test 
    @DisplayName("存在しないメールアドレス、ユーザーが取得されないこと")
    void testFindByMailAddressNotFound() {
        var result = userRepository.findByMailAddress(
            "nonexistent@example.invalid"
        );

        assertTrue(result.isEmpty());
    }

}
