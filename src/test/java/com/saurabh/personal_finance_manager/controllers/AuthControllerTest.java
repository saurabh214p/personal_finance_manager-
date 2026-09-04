package com.saurabh.personal_finance_manager.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.saurabh.personal_finance_manager.dtos.AuthDtos;
import com.saurabh.personal_finance_manager.exceptions.ApiException;
import com.saurabh.personal_finance_manager.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

  @Mock private AuthService authService;
  @Mock private AuthenticationManager authenticationManager;

  @InjectMocks private AuthController authController;

  @Test
  void testRegister() {
    AuthDtos.RegisterRequest req =
        new AuthDtos.RegisterRequest("user@test.com", "pass123", "User", "+1234567890");
    when(authService.register(req)).thenReturn(new AuthDtos.RegisterResponse("Success", 1L));

    ResponseEntity<AuthDtos.RegisterResponse> res = authController.register(req);
    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    assertEquals(1L, res.getBody().userId());
  }

  @Test
  void testLogin_Success() {
    AuthDtos.LoginRequest req = new AuthDtos.LoginRequest("user@test.com", "pass123");
    HttpServletRequest servletReq = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    Authentication auth = mock(Authentication.class);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenReturn(auth);
    when(servletReq.getSession(true)).thenReturn(session);

    AuthDtos.MessageResponse res = authController.login(req, servletReq);
    assertEquals("Login successful", res.message());
  }

  @Test
  void testLogin_Failure() {
    AuthDtos.LoginRequest req = new AuthDtos.LoginRequest("user@test.com", "wrongpass");
    HttpServletRequest servletReq = mock(HttpServletRequest.class);

    when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
        .thenThrow(new BadCredentialsException("Bad credentials"));

    assertThrows(ApiException.class, () -> authController.login(req, servletReq));
  }

  @Test
  void testLogout() {
    HttpServletRequest servletReq = mock(HttpServletRequest.class);
    HttpSession session = mock(HttpSession.class);
    when(servletReq.getSession(false)).thenReturn(session);

    AuthDtos.MessageResponse res = authController.logout(servletReq);
    verify(session).invalidate();
    assertEquals("Logout successful", res.message());
  }
}
