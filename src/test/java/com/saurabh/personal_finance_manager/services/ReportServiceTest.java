package com.saurabh.personal_finance_manager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.saurabh.personal_finance_manager.dtos.ReportDtos;
import com.saurabh.personal_finance_manager.entities.Category;
import com.saurabh.personal_finance_manager.entities.CategoryType;
import com.saurabh.personal_finance_manager.entities.Transaction;
import com.saurabh.personal_finance_manager.entities.User;
import com.saurabh.personal_finance_manager.exceptions.BadRequestException;
import com.saurabh.personal_finance_manager.repositories.TransactionRepository;
import com.saurabh.personal_finance_manager.security.CurrentUserProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

  @Mock private TransactionRepository transactionRepository;
  @Mock private CurrentUserProvider currentUserProvider;

  @InjectMocks private ReportServiceImpl reportService;

  private User mockUser;

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setId(1L);
    mockUser.setUsername("user@example.com");
  }

  @Test
  void testMonthlyReport_Success() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    Category incomeCat = new Category();
    incomeCat.setName("Salary");
    incomeCat.setType(CategoryType.INCOME);

    Category expenseCat = new Category();
    expenseCat.setName("Rent");
    expenseCat.setType(CategoryType.EXPENSE);

    Transaction t1 = new Transaction();
    t1.setAmount(BigDecimal.valueOf(5000));
    t1.setCategory(incomeCat);

    Transaction t2 = new Transaction();
    t2.setAmount(BigDecimal.valueOf(1500));
    t2.setCategory(expenseCat);

    when(transactionRepository.findByUserIdAndDeletedFalseAndDateBetween(
            eq(1L), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 1, 31))))
        .thenReturn(List.of(t1, t2));

    ReportDtos.MonthlyResponse res = reportService.monthly(2024, 1);

    assertEquals(1, res.month());
    assertEquals(2024, res.year());
    assertEquals(BigDecimal.valueOf(5000), res.totalIncome().get("Salary"));
    assertEquals(BigDecimal.valueOf(1500), res.totalExpenses().get("Rent"));
    assertEquals(BigDecimal.valueOf(3500), res.netSavings());
  }

  @Test
  void testMonthlyReport_InvalidMonth_ThrowsBadRequest() {
    assertThrows(BadRequestException.class, () -> reportService.monthly(2024, 0));
    assertThrows(BadRequestException.class, () -> reportService.monthly(2024, 13));
  }

  @Test
  void testYearlyReport_Success() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    Category incomeCat = new Category();
    incomeCat.setName("Salary");
    incomeCat.setType(CategoryType.INCOME);

    Transaction t1 = new Transaction();
    t1.setAmount(BigDecimal.valueOf(60000));
    t1.setCategory(incomeCat);

    when(transactionRepository.findByUserIdAndDeletedFalseAndDateBetween(
            eq(1L), eq(LocalDate.of(2024, 1, 1)), eq(LocalDate.of(2024, 12, 31))))
        .thenReturn(List.of(t1));

    ReportDtos.YearlyResponse res = reportService.yearly(2024);

    assertEquals(2024, res.year());
    assertEquals(BigDecimal.valueOf(60000), res.totalIncome().get("Salary"));
    assertTrue(res.totalExpenses().isEmpty());
    assertEquals(BigDecimal.valueOf(60000), res.netSavings());
  }
}
