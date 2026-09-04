package com.saurabh.personal_finance_manager.controllers;

import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.web.bind.annotation.*;

/** HTTP endpoints for registration, login and logout. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService service;
  private final AuthenticationManager authenticationManager;

  public AuthController(AuthService s, AuthenticationManager a) {
    service = s;
    authenticationManager = a;
  }

  @PostMapping("/register")
  public ResponseEntity<AuthDtos.RegisterResponse> register(
      @Valid @RequestBody AuthDtos.RegisterRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.register(r));
  }

  @PostMapping("/login")
  public AuthDtos.MessageResponse login(
      @Valid @RequestBody AuthDtos.LoginRequest r, HttpServletRequest request) {
    try {
      Authentication a =
          authenticationManager.authenticate(
              UsernamePasswordAuthenticationToken.unauthenticated(
                  r.username().trim().toLowerCase(), r.password()));
      SecurityContext c = SecurityContextHolder.createEmptyContext();
      c.setAuthentication(a);
      SecurityContextHolder.setContext(c);
      request.getSession(true).setAttribute("SPRING_SECURITY_CONTEXT", c);
      return new AuthDtos.MessageResponse("Login successful");
    } catch (AuthenticationException e) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }
  }

  @PostMapping("/logout")
  public AuthDtos.MessageResponse logout(HttpServletRequest request) {
    HttpSession s = request.getSession(false);
    if (s != null) s.invalidate();
    SecurityContextHolder.clearContext();
    return new AuthDtos.MessageResponse("Logout successful");
  }
}

