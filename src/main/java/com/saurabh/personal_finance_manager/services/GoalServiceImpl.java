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
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/** Computes live goal progress and enforces goal ownership. */
@Service
public class GoalServiceImpl implements GoalService {
  private final GoalRepository goals;
  private final TransactionRepository transactions;
  private final CurrentUserProvider current;
  private final CategoryRepository categories;

  public GoalServiceImpl(
      GoalRepository g,
      TransactionRepository t,
      CurrentUserProvider c,
      CategoryRepository cat) {
    goals = g;
    transactions = t;
    current = c;
    categories = cat;
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
    SavingsGoal saved = goals.save(g);

    if (r.categoryIds() != null && !r.categoryIds().isEmpty() && categories != null) {
      List<Long> distinctIds = r.categoryIds().stream().distinct().toList();
      List<Category> assignedCategories = new ArrayList<>();
      for (Long catId : distinctIds) {
        Category cat =
            categories
                .findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + catId));
        validateCategoryForGoal(cat, saved.getId());
        cat.setSavingsGoal(saved);
        categories.save(cat);
        assignedCategories.add(cat);
      }
      saved.setCategories(assignedCategories);
    }
    return response(saved);
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
    if (r.targetAmount() == null && r.targetDate() == null && r.categoryIds() == null)
      throw new BadRequestException("At least one goal field must be provided");
    if (r.targetDate() != null && !r.targetDate().isAfter(LocalDate.now()))
      throw new BadRequestException("targetDate must be in the future");
    SavingsGoal g = owned(id);
    if (r.targetAmount() != null) g.setTargetAmount(r.targetAmount());
    if (r.targetDate() != null) g.setTargetDate(r.targetDate());

    if (r.categoryIds() != null && categories != null) {
      List<Long> distinctIds = r.categoryIds().stream().distinct().toList();
      Set<Long> newCatIdSet = new HashSet<>(distinctIds);

      if (g.getCategories() != null) {
        for (Category existingCat : g.getCategories()) {
          if (!newCatIdSet.contains(existingCat.getId())) {
            existingCat.setSavingsGoal(null);
            categories.save(existingCat);
          }
        }
      }

      List<Category> newAssignedCategories = new ArrayList<>();
      for (Long catId : distinctIds) {
        Category cat =
            categories
                .findById(catId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + catId));
        validateCategoryForGoal(cat, g.getId());
        cat.setSavingsGoal(g);
        categories.save(cat);
        newAssignedCategories.add(cat);
      }
      g.setCategories(newAssignedCategories);
    }

    return response(goals.save(g));
  }

  public void delete(Long id) {
    SavingsGoal g = owned(id);
    if (categories != null && g.getCategories() != null) {
      for (Category c : g.getCategories()) {
        c.setSavingsGoal(null);
        categories.save(c);
      }
      g.getCategories().clear();
    }
    goals.delete(g);
  }

  private void validateCategoryForGoal(Category cat, Long goalId) {
    User u = current.current();
    if (cat.isCustom() && (cat.getUser() == null || !cat.getUser().getId().equals(u.getId()))) {
      throw new ForbiddenException("Category belongs to another user");
    }
    if (cat.getSavingsGoal() != null && !cat.getSavingsGoal().getId().equals(goalId)) {
      throw new ConflictException(
          "Category '" + cat.getName() + "' is already assigned to a savings goal");
    }
  }

  private SavingsGoal owned(Long id) {
    SavingsGoal g = goals.findById(id).orElseThrow(() -> new NotFoundException("Goal not found"));
    if (!g.getUser().getId().equals(current.current().getId()))
      throw new ForbiddenException("Goal belongs to another user");
    return g;
  }

  private GoalDtos.Response response(SavingsGoal g) {
    List<Category> cats = g.getCategories();
    BigDecimal p;
    List<Long> categoryIds;

    if (cats == null || cats.isEmpty()) {
      p = BigDecimal.ZERO;
      categoryIds = Collections.emptyList();
    } else {
      Set<Long> catIds = cats.stream().map(Category::getId).collect(Collectors.toSet());
      categoryIds = cats.stream().map(Category::getId).toList();

      p =
          transactions
              .findByUserIdAndDeletedFalseAndDateBetween(
                  g.getUser().getId(), g.getStartDate(), LocalDate.now())
              .stream()
              .filter(t -> t.getCategory() != null && catIds.contains(t.getCategory().getId()))
              .map(
                  t ->
                      t.getCategory().getType() == CategoryType.INCOME
                          ? t.getAmount()
                          : t.getAmount().negate())
              .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

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
        g.getTargetAmount().subtract(p),
        categoryIds);
  }
}
