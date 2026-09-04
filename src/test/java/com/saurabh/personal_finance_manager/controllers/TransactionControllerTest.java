package com.saurabh.personal_finance_manager.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.saurabh.personal_finance_manager.dtos.AuthDtos;
import com.saurabh.personal_finance_manager.dtos.TransactionDtos;
import com.saurabh.personal_finance_manager.entities.CategoryType;
import com.saurabh.personal_finance_manager.services.TransactionService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

  @Mock private TransactionService service;
  @InjectMocks private TransactionController controller;

  @Test
  void testCreate() {
    TransactionDtos.CreateRequest req =
        new TransactionDtos.CreateRequest(BigDecimal.valueOf(100), LocalDate.now(), "Salary", "Desc");
    TransactionDtos.Response resp =
        new TransactionDtos.Response(1L, BigDecimal.valueOf(100), LocalDate.now(), "Salary", "Desc", CategoryType.INCOME);

    when(service.create(req)).thenReturn(resp);

    ResponseEntity<TransactionDtos.Response> res = controller.create(req);
    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    assertEquals(1L, res.getBody().id());
  }

  @Test
  void testList() {
    when(service.list(null, null, null, null, null)).thenReturn(List.of());
    TransactionDtos.ListResponse res = controller.list(null, null, null, null, null);
    assertNotNull(res);
  }

  @Test
  void testUpdate() {
    TransactionDtos.UpdateRequest req =
        new TransactionDtos.UpdateRequest(BigDecimal.valueOf(200), null, null, null);
    TransactionDtos.Response resp =
        new TransactionDtos.Response(1L, BigDecimal.valueOf(200), LocalDate.now(), "Salary", "Desc", CategoryType.INCOME);

    when(service.update(1L, req)).thenReturn(resp);
    TransactionDtos.Response res = controller.update(1L, req);
    assertEquals(BigDecimal.valueOf(200), res.amount());
  }

  @Test
  void testDelete() {
    AuthDtos.MessageResponse res = controller.delete(1L);
    verify(service).delete(1L);
    assertEquals("Transaction deleted successfully", res.message());
  }
}
