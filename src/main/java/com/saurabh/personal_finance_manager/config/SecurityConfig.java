package com.saurabh.personal_finance_manager.config;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import org.springframework.context.annotation.*;
import org.springframework.http.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/** Central session-security configuration. */
@Configuration
public class SecurityConfig {
  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  UserDetailsService userDetailsService(UserRepository users) {
    return username ->
        users
            .findByUsername(username)
            .map(
                u ->
                    org.springframework.security.core.userdetails.User.withUsername(u.getUsername())
                        .password(u.getPassword())
                        .roles("USER")
                        .build())
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  @Bean
  org.springframework.security.authentication.AuthenticationManager authenticationManager(
      AuthenticationConfiguration c) throws Exception {
    return c.getAuthenticationManager();
  }

  @Bean
  SecurityFilterChain filterChain(HttpSecurity h) throws Exception {
    h.csrf(c -> c.disable())
        .authorizeHttpRequests(
            a ->
                a.requestMatchers(
                        "/api/auth/register",
                        "/api/auth/login",
                        "/h2-console/**",
                        "/v3/api-docs/**",
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/swagger-resources/**",
                        "/webjars/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .headers(x -> x.frameOptions(f -> f.sameOrigin()))
        .exceptionHandling(
            e ->
                e.authenticationEntryPoint(
                        (q, s, x) ->
                            write(
                                s,
                                HttpStatus.UNAUTHORIZED,
                                "Authentication required",
                                q.getRequestURI()))
                    .accessDeniedHandler(
                        (q, s, x) ->
                            write(s, HttpStatus.FORBIDDEN, "Access denied", q.getRequestURI())));
    h.sessionManagement(session -> session.sessionFixation(fixation -> fixation.none()));
    return h.build();
  }

  private void write(HttpServletResponse r, HttpStatus status, String message, String path)
      throws java.io.IOException {
    r.setStatus(status.value());
    r.setContentType(MediaType.APPLICATION_JSON_VALUE);
    new com.fasterxml.jackson.databind.ObjectMapper()
        .writeValue(
            r.getOutputStream(),
            new ErrorResponse(
                Instant.now(), status.value(), status.getReasonPhrase(), message, path));
  }
}
