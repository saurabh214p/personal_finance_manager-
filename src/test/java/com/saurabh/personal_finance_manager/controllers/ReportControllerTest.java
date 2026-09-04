package com.saurabh.personal_finance_manager.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.saurabh.personal_finance_manager.dtos.ReportDtos;
import com.saurabh.personal_finance_manager.services.ReportService;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportControllerTest {

  @Mock private ReportService service;
  @InjectMocks private ReportController controller;

  @Test
  void testMonthly() {
    ReportDtos.MonthlyResponse resp =
        new ReportDtos.MonthlyResponse(1, 2024, Map.of("Salary", BigDecimal.valueOf(5000)), Map.of(), BigDecimal.valueOf(5000));
    when(service.monthly(2024, 1)).thenReturn(resp);

    ReportDtos.MonthlyResponse res = controller.monthly(2024, 1);
    assertEquals(1, res.month());
    assertEquals(BigDecimal.valueOf(5000), res.netSavings());
  }

  @Test
  void testYearly() {
    ReportDtos.YearlyResponse resp =
        new ReportDtos.YearlyResponse(2024, Map.of("Salary", BigDecimal.valueOf(60000)), Map.of(), BigDecimal.valueOf(60000));
    when(service.yearly(2024)).thenReturn(resp);

    ReportDtos.YearlyResponse res = controller.yearly(2024);
    assertEquals(2024, res.year());
    assertEquals(BigDecimal.valueOf(60000), res.netSavings());
  }
}
