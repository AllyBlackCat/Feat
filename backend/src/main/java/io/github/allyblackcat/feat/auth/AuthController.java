package io.github.allyblackcat.feat.auth;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/signin")
    public void signin(@Valid @RequestBody SigninRequest request) {
        authService.signin(request.email(), request.password());
    }
}