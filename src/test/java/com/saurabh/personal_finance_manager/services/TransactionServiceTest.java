package com.saurabh.personal_finance_manager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.saurabh.personal_finance_manager.dtos.TransactionDtos;
import com.saurabh.personal_finance_manager.entities.Category;
import com.saurabh.personal_finance_manager.entities.CategoryType;
import com.saurabh.personal_finance_manager.entities.Transaction;
import com.saurabh.personal_finance_manager.entities.User;
import com.saurabh.personal_finance_manager.exceptions.BadRequestException;
import com.saurabh.personal_finance_manager.exceptions.ForbiddenException;
import com.saurabh.personal_finance_manager.exceptions.NotFoundException;
import com.saurabh.personal_finance_manager.repositories.TransactionRepository;
import com.saurabh.personal_finance_manager.security.CurrentUserProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

  @Mock private TransactionRepository transactionRepository;
  @Mock private CategoryService categoryService;
  @Mock private CurrentUserProvider currentUserProvider;

  @InjectMocks private TransactionServiceImpl transactionService;

  private User mockUser;
  private Category salaryCategory;

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setId(1L);
    mockUser.setUsername("user@example.com");

    salaryCategory = new Category();
    salaryCategory.setId(1L);
    salaryCategory.setName("Salary");
    salaryCategory.setType(CategoryType.INCOME);
  }

  @Test
  void testCreate_Success() {
    when(currentUserProvider.current()).thenReturn(mockUser);
    when(categoryService.resolveVisible("Salary")).thenReturn(salaryCategory);

    Transaction saved = new Transaction();
    saved.setId(100L);
    saved.setAmount(BigDecimal.valueOf(5000));
    saved.setDate(LocalDate.of(2024, 1, 15));
    saved.setCategory(salaryCategory);
    saved.setUser(mockUser);
    saved.setDescription("Monthly salary");

    when(transactionRepository.save(any(Transaction.class))).thenReturn(saved);

    TransactionDtos.CreateRequest request =
        new TransactionDtos.CreateRequest(BigDecimal.valueOf(5000), LocalDate.of(2024, 1, 15), "Salary", "Monthly salary");

    TransactionDtos.Response response = transactionService.create(request);

    assertNotNull(response);
    assertEquals(100L, response.id());
    assertEquals(BigDecimal.valueOf(5000), response.amount());
    assertEquals("Salary", response.category());
  }

  @Test
  void testCreate_FutureDate_ThrowsBadRequestException() {
    TransactionDtos.CreateRequest request =
        new TransactionDtos.CreateRequest(BigDecimal.valueOf(5000), LocalDate.now().plusDays(10), "Salary", "Future salary");

    assertThrows(BadRequestException.class, () -> transactionService.create(request));
  }

  @Test
  void testList_Filtering() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    Transaction t1 = new Transaction();
    t1.setId(1L);
    t1.setAmount(BigDecimal.valueOf(5000));
    t1.setDate(LocalDate.of(2024, 1, 15));
    t1.setCategory(salaryCategory);

    when(transactionRepository.findByUserIdAndDeletedFalseOrderByDateDescIdDesc(1L))
        .thenReturn(List.of(t1));

    List<TransactionDtos.Response> list =
        transactionService.list(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 1, 31),
            1L,
            "Salary",
            CategoryType.INCOME);

    assertEquals(1, list.size());
    assertEquals(1L, list.get(0).id());
  }

  @Test
  void testList_InvalidDateRange_ThrowsBadRequestException() {
    assertThrows(
        BadRequestException.class,
        () -> transactionService.list(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 1, 1), null, null, null));
  }

  @Test
  void testUpdate_Success() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    Transaction existing = new Transaction();
    existing.setId(1L);
    existing.setUser(mockUser);
    existing.setAmount(BigDecimal.valueOf(1000));
    existing.setDate(LocalDate.of(2024, 1, 15));
    existing.setCategory(salaryCategory);

    when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));
    when(transactionRepository.save(any(Transaction.class))).thenAnswer(inv -> inv.getArgument(0));

    TransactionDtos.UpdateRequest updateRequest =
        new TransactionDtos.UpdateRequest(BigDecimal.valueOf(2000), null, "Updated desc", LocalDate.of(2024, 5, 1));

    TransactionDtos.Response res = transactionService.update(1L, updateRequest);

    assertEquals(BigDecimal.valueOf(2000), res.amount());
    assertEquals("Updated desc", res.description());
    assertEquals(LocalDate.of(2024, 1, 15), res.date()); // date is immutable
  }

  @Test
  void testDelete_SoftDelete() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    Transaction existing = new Transaction();
    existing.setId(1L);
    existing.setUser(mockUser);
    existing.setCategory(salaryCategory);

    when(transactionRepository.findById(1L)).thenReturn(Optional.of(existing));

    transactionService.delete(1L);

    assertTrue(existing.isDeleted());
    verify(transactionRepository).save(existing);
  }

  @Test
  void testOwned_ForbiddenWhenOtherUser() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    User otherUser = new User();
    otherUser.setId(2L);

    Transaction otherTransaction = new Transaction();
    otherTransaction.setId(10L);
    otherTransaction.setUser(otherUser);

    when(transactionRepository.findById(10L)).thenReturn(Optional.of(otherTransaction));

    assertThrows(ForbiddenException.class, () -> transactionService.delete(10L));
  }

  @Test
  void testOwned_NotFoundWhenNotExists() {
    when(transactionRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> transactionService.delete(99L));
  }
}
