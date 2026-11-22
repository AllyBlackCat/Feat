
package io.github.allyblackcat.feat.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

class SessionTokenServiceTest {

    private final SessionTokenService service =
        new SessionTokenService();

    @Test
    @DisplayName("異なるトークンを生成する")
    void generateDifferentTokens() {
        String first = service.generateToken();
        String second = service.generateToken();

        assertEquals(64, first.length());
        assertEquals(64, second.length());
        assertNotEquals(first, second);
    }

    @Test
    @DisplayName("同じトークンから同じハッシュ値を生成する")
    void hashTokenConsistently() {
        String token = service.generateToken();

        String first = service.hashToken(token);
        String second = service.hashToken(token);

        assertEquals(64, first.length());
        assertEquals(first, second);
        assertNotEquals(token, first);
    }
}
