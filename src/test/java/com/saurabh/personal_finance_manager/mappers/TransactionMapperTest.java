package com.saurabh.personal_finance_manager.mappers;

import static org.junit.jupiter.api.Assertions.*;

import com.saurabh.personal_finance_manager.dtos.TransactionDtos;
import com.saurabh.personal_finance_manager.entities.Category;
import com.saurabh.personal_finance_manager.entities.CategoryType;
import com.saurabh.personal_finance_manager.entities.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TransactionMapperTest {

  @Test
  void testResponseMapping() {
    Category cat = new Category();
    cat.setName("Salary");
    cat.setType(CategoryType.INCOME);

    Transaction t = new Transaction();
    t.setId(1L);
    t.setAmount(BigDecimal.valueOf(5000));
    t.setDate(LocalDate.of(2024, 1, 15));
    t.setCategory(cat);
    t.setDescription("Test");

    TransactionDtos.Response res = TransactionMapper.response(t);

    assertEquals(1L, res.id());
    assertEquals(BigDecimal.valueOf(5000), res.amount());
    assertEquals(LocalDate.of(2024, 1, 15), res.date());
    assertEquals("Salary", res.category());
    assertEquals(CategoryType.INCOME, res.type());
    assertEquals("Test", res.description());
  }

  @Test
  void testApply() {
    Category cat = new Category();
    cat.setName("Food");
    cat.setType(CategoryType.EXPENSE);

    TransactionDtos.CreateRequest req =
        new TransactionDtos.CreateRequest(BigDecimal.valueOf(100), LocalDate.of(2024, 1, 20), "Food", "Dinner");

    Transaction t = new Transaction();
    TransactionMapper.apply(t, req, cat);

    assertEquals(BigDecimal.valueOf(100), t.getAmount());
    assertEquals(LocalDate.of(2024, 1, 20), t.getDate());
    assertEquals(cat, t.getCategory());
    assertEquals("Dinner", t.getDescription());
  }
}
