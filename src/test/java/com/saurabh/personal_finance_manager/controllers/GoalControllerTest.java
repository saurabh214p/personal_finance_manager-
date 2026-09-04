package com.saurabh.personal_finance_manager.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.saurabh.personal_finance_manager.dtos.AuthDtos;
import com.saurabh.personal_finance_manager.dtos.GoalDtos;
import com.saurabh.personal_finance_manager.services.GoalService;
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
class GoalControllerTest {

  @Mock private GoalService service;
  @InjectMocks private GoalController controller;

  @Test
  void testCreate() {
    GoalDtos.CreateRequest req =
        new GoalDtos.CreateRequest("Car", BigDecimal.valueOf(10000), LocalDate.now().plusMonths(6), null);
    GoalDtos.Response resp =
        new GoalDtos.Response(1L, "Car", BigDecimal.valueOf(10000), LocalDate.now().plusMonths(6), LocalDate.now(), BigDecimal.ZERO, 0.0, BigDecimal.valueOf(10000));

    when(service.create(req)).thenReturn(resp);

    ResponseEntity<GoalDtos.Response> res = controller.create(req);
    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    assertEquals("Car", res.getBody().goalName());
  }

  @Test
  void testList() {
    when(service.list()).thenReturn(List.of());
    GoalDtos.ListResponse res = controller.list();
    assertNotNull(res);
  }

  @Test
  void testGet() {
    GoalDtos.Response resp =
        new GoalDtos.Response(1L, "Car", BigDecimal.valueOf(10000), LocalDate.now().plusMonths(6), LocalDate.now(), BigDecimal.ZERO, 0.0, BigDecimal.valueOf(10000));
    when(service.get(1L)).thenReturn(resp);

    GoalDtos.Response res = controller.get(1L);
    assertEquals(1L, res.id());
  }

  @Test
  void testUpdate() {
    GoalDtos.UpdateRequest req = new GoalDtos.UpdateRequest(BigDecimal.valueOf(12000), null);
    GoalDtos.Response resp =
        new GoalDtos.Response(1L, "Car", BigDecimal.valueOf(12000), LocalDate.now().plusMonths(6), LocalDate.now(), BigDecimal.ZERO, 0.0, BigDecimal.valueOf(12000));
    when(service.update(1L, req)).thenReturn(resp);

    GoalDtos.Response res = controller.update(1L, req);
    assertEquals(BigDecimal.valueOf(12000), res.targetAmount());
  }

  @Test
  void testDelete() {
    AuthDtos.MessageResponse res = controller.delete(1L);
    verify(service).delete(1L);
    assertEquals("Goal deleted successfully", res.message());
  }
}
