package com.saurabh.personal_finance_manager.dtos;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import java.math.BigDecimal;
import java.util.Map;

/** Report response DTOs. */
public final class ReportDtos {
  private ReportDtos() {}

  public record MonthlyResponse(
      int month,
      int year,
      Map<String, BigDecimal> totalIncome,
      Map<String, BigDecimal> totalExpenses,
      BigDecimal netSavings) {}

  public record YearlyResponse(
      int year,
      Map<String, BigDecimal> totalIncome,
      Map<String, BigDecimal> totalExpenses,
      BigDecimal netSavings) {}
}

