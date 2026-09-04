package com.saurabh.personal_finance_manager.dtos;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Transaction request and response DTOs. */
public final class TransactionDtos {
  private TransactionDtos() {}

  public record CreateRequest(
      @NotNull @Positive BigDecimal amount,
      @NotNull LocalDate date,
      @NotBlank String category,
      String description) {}

  public record UpdateRequest(
      @Positive BigDecimal amount, String category, String description, LocalDate date) {}

  public record Response(
      Long id,
      BigDecimal amount,
      LocalDate date,
      String category,
      String description,
      CategoryType type) {}

  public record ListResponse(List<Response> transactions) {}
}

