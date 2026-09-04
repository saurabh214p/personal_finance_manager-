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

/** HTTP endpoints for savings goals. */
@RestController
@RequestMapping("/api/goals")
public class GoalController {
  private final GoalService service;

  public GoalController(GoalService s) {
    service = s;
  }

  @PostMapping
  public ResponseEntity<GoalDtos.Response> create(@Valid @RequestBody GoalDtos.CreateRequest r) {
    return ResponseEntity.status(HttpStatus.CREATED).body(service.create(r));
  }

  @GetMapping
  public GoalDtos.ListResponse list() {
    return new GoalDtos.ListResponse(service.list());
  }

  @GetMapping("/{id}")
  public GoalDtos.Response get(@PathVariable Long id) {
    return service.get(id);
  }

  @PutMapping("/{id}")
  public GoalDtos.Response update(
      @PathVariable Long id, @Valid @RequestBody GoalDtos.UpdateRequest r) {
    return service.update(id, r);
  }

  @DeleteMapping("/{id}")
  public AuthDtos.MessageResponse delete(@PathVariable Long id) {
    service.delete(id);
    return new AuthDtos.MessageResponse("Goal deleted successfully");
  }
}

