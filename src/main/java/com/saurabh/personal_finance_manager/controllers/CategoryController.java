package com.saurabh.personal_finance_manager.controllers;

import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** HTTP endpoints for visible categories. */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {
  private final CategoryService service;

  public CategoryController(CategoryService s) {
    service = s;
  }

  @GetMapping
  public CategoryDtos.ListResponse list() {
    return new CategoryDtos.ListResponse(service.list());
  }

  @PostMapping
  public ResponseEntity<CategoryDtos.Response> create(
      @Valid @RequestBody CategoryDtos.CreateRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
  }

  @DeleteMapping("/{name}")
  public AuthDtos.MessageResponse delete(@PathVariable String name) {
    service.delete(name);
    return new AuthDtos.MessageResponse("Category deleted successfully");
  }
}

