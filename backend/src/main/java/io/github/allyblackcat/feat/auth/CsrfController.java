package io.github.allyblackcat.feat.auth;

import java.util.Map;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CsrfController {

    @GetMapping("/api/auth/csrf")
    public Map<String, String> getCsrfToken(
            CsrfToken csrfToken) {

        return Map.of(
            "token",
            csrfToken.getToken()
        );
    }
}
