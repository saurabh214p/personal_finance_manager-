package com.saurabh.personal_finance_manager.controllers;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.saurabh.personal_finance_manager.dtos.AuthDtos;
import com.saurabh.personal_finance_manager.dtos.CategoryDtos;
import com.saurabh.personal_finance_manager.entities.CategoryType;
import com.saurabh.personal_finance_manager.services.CategoryService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

  @Mock private CategoryService service;
  @InjectMocks private CategoryController controller;

  @Test
  void testList() {
    when(service.list()).thenReturn(List.of(new CategoryDtos.Response(1L, "Salary", CategoryType.INCOME, false)));
    CategoryDtos.ListResponse res = controller.list();
    assertEquals(1, res.categories().size());
  }

  @Test
  void testCreate() {
    CategoryDtos.CreateRequest req = new CategoryDtos.CreateRequest("Bonus", CategoryType.INCOME);
    when(service.create(req)).thenReturn(new CategoryDtos.Response(2L, "Bonus", CategoryType.INCOME, true));

    ResponseEntity<CategoryDtos.Response> res = controller.create(req);
    assertEquals(HttpStatus.CREATED, res.getStatusCode());
    assertEquals("Bonus", res.getBody().name());
  }

  @Test
  void testDelete() {
    AuthDtos.MessageResponse res = controller.delete("Bonus");
    verify(service).delete("Bonus");
    assertEquals("Category deleted successfully", res.message());
  }
}
