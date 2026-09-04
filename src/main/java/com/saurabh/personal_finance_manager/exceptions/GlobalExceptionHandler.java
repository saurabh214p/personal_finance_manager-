package com.saurabh.personal_finance_manager.exceptions;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

/** Converts expected exceptions and Bean Validation failures into API errors. */
@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(ApiException.class)
  ResponseEntity<ErrorResponse> api(ApiException e, HttpServletRequest r) {
    return response(e.getStatus(), e.getMessage(), r);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ErrorResponse> validation(
      MethodArgumentNotValidException e, HttpServletRequest r) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(x -> x.getField() + " " + x.getDefaultMessage())
            .orElse("Invalid request");
    return response(HttpStatus.BAD_REQUEST, message, r);
  }

  @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
  ResponseEntity<ErrorResponse> malformed(Exception e, HttpServletRequest r) {
    return response(HttpStatus.BAD_REQUEST, "Malformed request", r);
  }

  private ResponseEntity<ErrorResponse> response(
      HttpStatus status, String message, HttpServletRequest r) {
    return ResponseEntity.status(status)
        .body(
            new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                r.getRequestURI()));
  }
}

