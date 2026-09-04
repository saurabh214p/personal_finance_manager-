package com.saurabh.personal_finance_manager.exceptions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.saurabh.personal_finance_manager.dtos.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

  private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

  @Test
  void testApiExceptionHandling() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/api/test");

    BadRequestException ex = new BadRequestException("Invalid input");
    ResponseEntity<ErrorResponse> res = handler.api(ex, req);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    assertNotNull(res.getBody());
    assertEquals("Invalid input", res.getBody().message());
    assertEquals("/api/test", res.getBody().path());
  }

  @Test
  void testMalformedHandling() {
    HttpServletRequest req = mock(HttpServletRequest.class);
    when(req.getRequestURI()).thenReturn("/api/test");

    ResponseEntity<ErrorResponse> res = handler.malformed(new Exception("Malformed"), req);

    assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
    assertNotNull(res.getBody());
    assertEquals("Malformed request", res.getBody().message());
  }
}
