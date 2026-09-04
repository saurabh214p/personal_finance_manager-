package com.saurabh.personal_finance_manager.controllers;

import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** HTTP endpoints for the authenticated user's transactions. */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {
  private final TransactionService service;

  public TransactionController(TransactionService s) {
    service = s;
  }

  @PostMapping
  public ResponseEntity<TransactionDtos.Response> create(
      @Valid @RequestBody TransactionDtos.CreateRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
  }

  @GetMapping
  public TransactionDtos.ListResponse list(
      @RequestParam(required = false) LocalDate startDate,
      @RequestParam(required = false) LocalDate endDate,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) CategoryType type) {
    return new TransactionDtos.ListResponse(
        service.list(startDate, endDate, categoryId, category, type));
  }

  @PutMapping("/{id}")
  public TransactionDtos.Response update(
      @PathVariable Long id, @Valid @RequestBody TransactionDtos.UpdateRequest r) {
    return service.update(id, r);
  }

  @DeleteMapping("/{id}")
  public AuthDtos.MessageResponse delete(@PathVariable Long id) {
    service.delete(id);
    return new AuthDtos.MessageResponse("Transaction deleted successfully");
  }
}

