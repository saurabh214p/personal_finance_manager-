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

/** Goal request and response DTOs. */
public final class GoalDtos {
  private GoalDtos() {}

  public record CreateRequest(
      @NotBlank String goalName,
      @NotNull @Positive BigDecimal targetAmount,
      @NotNull LocalDate targetDate,
      LocalDate startDate,
      List<Long> categoryIds) {

    public CreateRequest(
        String goalName, BigDecimal targetAmount, LocalDate targetDate, LocalDate startDate) {
      this(goalName, targetAmount, targetDate, startDate, null);
    }
  }

  public record UpdateRequest(
      @Positive BigDecimal targetAmount,
      LocalDate targetDate,
      List<Long> categoryIds) {

    public UpdateRequest(BigDecimal targetAmount, LocalDate targetDate) {
      this(targetAmount, targetDate, null);
    }
  }

  public record Response(
      Long id,
      String goalName,
      BigDecimal targetAmount,
      LocalDate targetDate,
      LocalDate startDate,
      BigDecimal currentProgress,
      Double progressPercentage,
      BigDecimal remainingAmount,
      List<Long> categoryIds) {

    public Response(
        Long id,
        String goalName,
        BigDecimal targetAmount,
        LocalDate targetDate,
        LocalDate startDate,
        BigDecimal currentProgress,
        Double progressPercentage,
        BigDecimal remainingAmount) {
      this(
          id,
          goalName,
          targetAmount,
          targetDate,
          startDate,
          currentProgress,
          progressPercentage,
          remainingAmount,
          List.of());
    }
  }

  public record ListResponse(List<Response> goals) {}
}
