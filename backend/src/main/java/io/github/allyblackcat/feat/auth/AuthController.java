package io.github.allyblackcat.feat.auth;

import java.time.Duration;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final SigninSessionService signinSessionService;

    public AuthController(
            SigninSessionService signinSessionService) {

        this.signinSessionService = signinSessionService;
    }

    @PostMapping("/api/auth/signin")
    public ResponseEntity<Void> signin(
            @Valid @RequestBody SigninRequest request) {

        String token = signinSessionService.signin(
            request.email(),
            request.password()
        );

        ResponseCookie cookie = ResponseCookie
            .from("FEAT_SESSION", token)
            .httpOnly(true)
            .secure(true)
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofHours(8))
            .build();

        return ResponseEntity.ok()
            .header(
                HttpHeaders.SET_COOKIE,
                cookie.toString()
            )
            .build();
    }
}
