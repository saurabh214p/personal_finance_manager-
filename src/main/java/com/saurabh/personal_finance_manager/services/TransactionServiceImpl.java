package com.saurabh.personal_finance_manager.services;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.stereotype.Service;

/** Implements transaction validation, isolation and soft deletion. */
@Service
public class TransactionServiceImpl implements TransactionService {
  private final TransactionRepository transactions;
  private final CategoryService categories;
  private final CurrentUserProvider current;

  public TransactionServiceImpl(TransactionRepository t, CategoryService c, CurrentUserProvider u) {
    transactions = t;
    categories = c;
    current = u;
  }

  public TransactionDtos.Response create(TransactionDtos.CreateRequest r) {
    if (r.date().isAfter(LocalDate.now()))
      throw new BadRequestException("date cannot be in the future");
    User u = current.current();
    Transaction t = new Transaction();
    TransactionMapper.apply(t, r, categories.resolveVisible(r.category()));
    t.setUser(u);
    return TransactionMapper.response(transactions.save(t));
  }

  public List<TransactionDtos.Response> list(
      LocalDate start, LocalDate end, Long categoryId, CategoryType type) {
    return list(start, end, categoryId, null, type);
  }

  public List<TransactionDtos.Response> list(
      LocalDate start, LocalDate end, Long categoryId, String category, CategoryType type) {
    if (start != null && end != null && start.isAfter(end))
      throw new BadRequestException("startDate must not be after endDate");
    return transactions
        .findByUserIdAndDeletedFalseOrderByDateDescIdDesc(current.current().getId())
        .stream()
        .filter(t -> start == null || !t.getDate().isBefore(start))
        .filter(t -> end == null || !t.getDate().isAfter(end))
        .filter(t -> categoryId == null || t.getCategory().getId().equals(categoryId))
        .filter(
            t ->
                category == null
                    || category.isBlank()
                    || t.getCategory().getName().equalsIgnoreCase(category.trim())
                    || String.valueOf(t.getCategory().getId()).equals(category.trim()))
        .filter(t -> type == null || t.getCategory().getType() == type)
        .map(TransactionMapper::response)
        .toList();
  }

  public TransactionDtos.Response update(Long id, TransactionDtos.UpdateRequest r) {
    Transaction t = owned(id);
    if (r.amount() != null) t.setAmount(r.amount());
    if (r.category() != null && !r.category().isBlank())
      t.setCategory(categories.resolveVisible(r.category()));
    if (r.description() != null) t.setDescription(r.description());
    return TransactionMapper.response(transactions.save(t));
  }

  public void delete(Long id) {
    Transaction t = owned(id);
    t.setDeleted(true);
    transactions.save(t);
  }

  private Transaction owned(Long id) {
    Transaction t =
        transactions.findById(id).orElseThrow(() -> new NotFoundException("Transaction not found"));
    if (!t.getUser().getId().equals(current.current().getId()))
      throw new ForbiddenException("Transaction belongs to another user");
    return t;
  }
}
