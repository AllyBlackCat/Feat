package io.github.allyblackcat.feat.auth;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.validation.Valid;

@RestController
public class AuthController {
    @PostMapping("/api/auth/signin")
    public void signin(@Valid @RequestBody SigninRequest request) {
    }
}