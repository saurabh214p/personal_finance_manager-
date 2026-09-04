package com.saurabh.personal_finance_manager.services;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** Aggregates only the current user's active transactions. */
@Service
public class ReportServiceImpl implements ReportService {
  private final TransactionRepository transactions;
  private final CurrentUserProvider current;

  public ReportServiceImpl(TransactionRepository t, CurrentUserProvider c) {
    transactions = t;
    current = c;
  }

  public ReportDtos.MonthlyResponse monthly(int year, int month) {
    if (month < 1 || month > 12) throw new BadRequestException("month must be between 1 and 12");
    LocalDate s = LocalDate.of(year, month, 1), e = s.withDayOfMonth(s.lengthOfMonth());
    Data d = aggregate(s, e);
    return new ReportDtos.MonthlyResponse(month, year, d.income, d.expenses, d.net());
  }

  public ReportDtos.YearlyResponse yearly(int year) {
    Data d = aggregate(LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31));
    return new ReportDtos.YearlyResponse(year, d.income, d.expenses, d.net());
  }

  private Data aggregate(LocalDate s, LocalDate e) {
    Map<String, BigDecimal> income = new LinkedHashMap<>(), expenses = new LinkedHashMap<>();
    transactions
        .findByUserIdAndDeletedFalseAndDateBetween(current.current().getId(), s, e)
        .forEach(
            t -> {
              Map<String, BigDecimal> m =
                  t.getCategory().getType() == CategoryType.INCOME ? income : expenses;
              m.merge(t.getCategory().getName(), t.getAmount(), BigDecimal::add);
            });
    return new Data(income, expenses);
  }

  private record Data(Map<String, BigDecimal> income, Map<String, BigDecimal> expenses) {
    BigDecimal net() {
      return income.values().stream()
          .reduce(BigDecimal.ZERO, BigDecimal::add)
          .subtract(expenses.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
    }
  }
}

