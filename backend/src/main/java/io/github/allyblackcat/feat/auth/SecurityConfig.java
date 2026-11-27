package io.github.allyblackcat.feat.auth;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.http.HttpStatus;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        CookieCsrfTokenRepository csrfRepository =
            new CookieCsrfTokenRepository();

        csrfRepository.setCookieName("__Host-XSRF-TOKEN");
        csrfRepository.setCookieCustomizer(cookie -> cookie
            .path("/")
            .secure(true)
            .sameSite("Lax")
        );

        http
            .httpBasic(config -> config.disable())
            .formLogin(config -> config.disable())
            .logout(config -> config.disable())

            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/auth/**",
                    "/actuator/health"
                ).permitAll()
                .anyRequest().denyAll()
            )

            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfRepository)
                .csrfTokenRequestHandler(
                    new CsrfTokenRequestAttributeHandler()
                )
                .ignoringRequestMatchers(
                    "/api/auth/signin"
                )
            )

            .exceptionHandling(exceptions ->
                exceptions.accessDeniedHandler(
                    (request, response, exception) -> {

                        if (!(exception instanceof CsrfException)) {
                            response.sendError(
                                HttpStatus.FORBIDDEN.value()
                            );
                            return;
                        }

                        response.setStatus(
                            HttpStatus.FORBIDDEN.value()
                        );

                        response.setContentType(
                            "application/json;charset=UTF-8"
                        );

                        response.getWriter().write("""
                            {
                                "code": "ERROR-00001",
                                "message": "CSRF検証に失敗しました。"
                            }
                            """);
                    }
                )
            );

        return http.build();
    }
}
