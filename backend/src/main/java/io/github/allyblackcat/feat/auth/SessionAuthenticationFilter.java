package io.github.allyblackcat.feat.auth;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class SessionAuthenticationFilter
        extends OncePerRequestFilter {

    private final MeService meService;

    public SessionAuthenticationFilter(MeService meService) {
        this.meService = meService;
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {

        String path = request.getServletPath();

        return !path.startsWith("/api/")
            || path.startsWith("/api/auth/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String token = getSessionToken(request);

        if (token != null && !token.isBlank()) {
            try {
                MeResponse user = meService.getMe(token);

                String role = user.administrator()
                    ? "ROLE_ADMIN"
                    : "ROLE_USER";

                var authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                        user,
                        null,
                        List.of(new SimpleGrantedAuthority(role))
                    );

                SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);

            } catch (InvalidSessionException exception) {
                // 無効なセッションは未認証として扱う。
                SecurityContextHolder.clearContext();
            }
        }

        chain.doFilter(request, response);
    }

    private String getSessionToken(
            HttpServletRequest request) {

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if ("FEAT_SESSION".equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}
