package io.github.allyblackcat.feat.auth;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;

import io.github.allyblackcat.feat.user.User;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/signin")
    public void signin(
            @Valid @RequestBody SigninRequest request, 
            HttpServletRequest httpRequest) {

        User user = authService.signin(
            request.email(), 
            request.password()
        );

        HttpSession session = httpRequest.getSession(true);
        httpRequest.changeSessionId();
        session.setAttribute("USER_ID", user.id());
    }
}