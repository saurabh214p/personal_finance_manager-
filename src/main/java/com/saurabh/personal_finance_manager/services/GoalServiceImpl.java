package com.saurabh.personal_finance_manager.services;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import java.math.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;

/** Computes live goal progress and enforces goal ownership. */
@Service
public class GoalServiceImpl implements GoalService {
  private final GoalRepository goals;
  private final TransactionRepository transactions;
  private final CurrentUserProvider current;

  public GoalServiceImpl(GoalRepository g, TransactionRepository t, CurrentUserProvider c) {
    goals = g;
    transactions = t;
    current = c;
  }

  public GoalDtos.Response create(GoalDtos.CreateRequest r) {
    if (!r.targetDate().isAfter(LocalDate.now()))
      throw new BadRequestException("targetDate must be in the future");
    if (r.startDate() != null && r.startDate().isAfter(r.targetDate()))
      throw new BadRequestException("startDate must not be after targetDate");
    SavingsGoal g = new SavingsGoal();
    g.setGoalName(r.goalName().trim());
    g.setTargetAmount(r.targetAmount());
    g.setTargetDate(r.targetDate());
    g.setStartDate(r.startDate() == null ? LocalDate.now() : r.startDate());
    g.setUser(current.current());
    return response(goals.save(g));
  }

  public List<GoalDtos.Response> list() {
    return goals.findByUserIdOrderByIdDesc(current.current().getId()).stream()
        .map(this::response)
        .toList();
  }

  public GoalDtos.Response get(Long id) {
    return response(owned(id));
  }

  public GoalDtos.Response update(Long id, GoalDtos.UpdateRequest r) {
    if (r.targetAmount() == null && r.targetDate() == null)
      throw new BadRequestException("At least one goal field must be provided");
    if (r.targetDate() != null && !r.targetDate().isAfter(LocalDate.now()))
      throw new BadRequestException("targetDate must be in the future");
    SavingsGoal g = owned(id);
    if (r.targetAmount() != null) g.setTargetAmount(r.targetAmount());
    if (r.targetDate() != null) g.setTargetDate(r.targetDate());
    return response(goals.save(g));
  }

  public void delete(Long id) {
    goals.delete(owned(id));
  }

  private SavingsGoal owned(Long id) {
    SavingsGoal g = goals.findById(id).orElseThrow(() -> new NotFoundException("Goal not found"));
    if (!g.getUser().getId().equals(current.current().getId()))
      throw new ForbiddenException("Goal belongs to another user");
    return g;
  }

  private GoalDtos.Response response(SavingsGoal g) {
    BigDecimal p =
        transactions
            .findByUserIdAndDeletedFalseAndDateBetween(
                g.getUser().getId(), g.getStartDate(), LocalDate.now())
            .stream()
            .map(
                t ->
                    t.getCategory().getType() == CategoryType.INCOME
                        ? t.getAmount()
                        : t.getAmount().negate())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal percentage =
        p.multiply(BigDecimal.valueOf(100)).divide(g.getTargetAmount(), 2, RoundingMode.HALF_UP);
    return new GoalDtos.Response(
        g.getId(),
        g.getGoalName(),
        g.getTargetAmount(),
        g.getTargetDate(),
        g.getStartDate(),
        p,
        percentage.doubleValue(),
        g.getTargetAmount().subtract(p));
  }
}
