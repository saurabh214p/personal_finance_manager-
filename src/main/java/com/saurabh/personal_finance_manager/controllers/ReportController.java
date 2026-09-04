package com.saurabh.personal_finance_manager.controllers;

import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import org.springframework.web.bind.annotation.*;

/** HTTP endpoints for monthly and yearly reports. */
@RestController
@RequestMapping("/api/reports")
public class ReportController {
  private final ReportService service;

  public ReportController(ReportService s) {
    service = s;
  }

  @GetMapping("/monthly/{year}/{month}")
  public ReportDtos.MonthlyResponse monthly(@PathVariable int year, @PathVariable int month) {
    return service.monthly(year, month);
  }

  @GetMapping("/yearly/{year}")
  public ReportDtos.YearlyResponse yearly(@PathVariable int year) {
    return service.yearly(year);
  }
}

