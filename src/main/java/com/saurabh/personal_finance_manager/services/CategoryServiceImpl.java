package com.saurabh.personal_finance_manager.services;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import java.util.*;
import org.springframework.stereotype.Service;

/** Implements category ownership, visibility and deletion rules. */
@Service
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepository categories;
  private final TransactionRepository transactions;
  private final CurrentUserProvider current;

  public CategoryServiceImpl(CategoryRepository c, TransactionRepository t, CurrentUserProvider u) {
    categories = c;
    transactions = t;
    current = u;
  }

  public CategoryDtos.Response create(CategoryDtos.CreateRequest r) {
    User u = current.current();
    String n = r.name().trim();
    if (categories.existsByNameIgnoreCaseAndUserId(n, u.getId()))
      throw new ConflictException("Category name already exists");
    Category c = new Category();
    c.setName(n);
    c.setType(r.type());
    c.setCustom(true);
    c.setUser(u);
    return response(categories.save(c));
  }

  public List<CategoryDtos.Response> list() {
    return categories.findByCustomFalseOrUserIdOrderByNameAsc(current.current().getId()).stream()
        .map(this::response)
        .toList();
  }

  public void delete(String name) {
    User u = current.current();
    Category c =
        categories
            .findByNameIgnoreCaseAndCustomFalse(name)
            .orElseGet(
                () ->
                    categories
                        .findByNameIgnoreCaseAndUserId(name, u.getId())
                        .orElseThrow(() -> new NotFoundException("Category not found")));
    if (!c.isCustom()) throw new ForbiddenException("Default categories cannot be deleted");
    if (!c.getUser().getId().equals(u.getId()))
      throw new ForbiddenException("Category belongs to another user");
    if (transactions.existsByCategoryIdAndDeletedFalse(c.getId()))
      throw new ConflictException("Category is in use by transactions");
    categories.delete(c);
  }

  public Category resolveVisible(String name) {
    User u = current.current();
    return categories
        .findByNameIgnoreCaseAndCustomFalse(name)
        .or(() -> categories.findByNameIgnoreCaseAndUserId(name, u.getId()))
        .orElseThrow(() -> new BadRequestException("Category is invalid or unavailable"));
  }

  private CategoryDtos.Response response(Category c) {
    return new CategoryDtos.Response(c.getId(), c.getName(), c.getType(), c.isCustom());
  }
}

