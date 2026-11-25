package io.github.allyblackcat.feat.auth;

import java.time.Duration;
import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    private final SigninSessionService signinSessionService;
    private final MeService meService;

    public AuthController(
            SigninSessionService signinSessionService,
            MeService meService) {

        this.signinSessionService = signinSessionService;
        this.meService = meService;
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

    @GetMapping("/api/auth/me")
    public ResponseEntity<MeResponse> me(
            @CookieValue(
                value = "FEAT_SESSION",
                required = false
            ) String token) {

        MeResponse response = meService.getMe(token);

        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(InvalidSessionException.class)
    public ResponseEntity<Map<String, String>> handleInvalidSession(
            InvalidSessionException exception) {

        Map<String, String> body = Map.of(
            "code", "ERROR-01009",
            "message", "認証に失敗しました。"
        );

        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(body);
    }
}
