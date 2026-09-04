package com.saurabh.personal_finance_manager.dtos;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import jakarta.validation.constraints.*;

/** API request and response DTOs for authentication. */
public final class AuthDtos {
  private AuthDtos() {}

  public record RegisterRequest(
      @NotBlank @Email String username,
      @NotBlank @Size(min = 6) String password,
      @NotBlank String fullName,
      @NotBlank @Pattern(regexp = "^[+0-9() -]{6,30}$", message = "must be a valid phone number")
          String phoneNumber) {}

  public record LoginRequest(@NotBlank @Email String username, @NotBlank String password) {}

  public record MessageResponse(String message) {}

  public record RegisterResponse(String message, Long userId) {}
}

