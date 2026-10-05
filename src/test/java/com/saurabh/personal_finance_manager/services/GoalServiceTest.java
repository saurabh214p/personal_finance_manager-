package com.saurabh.personal_finance_manager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.saurabh.personal_finance_manager.dtos.GoalDtos;
import com.saurabh.personal_finance_manager.entities.Category;
import com.saurabh.personal_finance_manager.entities.CategoryType;
import com.saurabh.personal_finance_manager.entities.SavingsGoal;
import com.saurabh.personal_finance_manager.entities.Transaction;
import com.saurabh.personal_finance_manager.entities.User;
import com.saurabh.personal_finance_manager.exceptions.BadRequestException;
import com.saurabh.personal_finance_manager.exceptions.ConflictException;
import com.saurabh.personal_finance_manager.exceptions.ForbiddenException;
import com.saurabh.personal_finance_manager.exceptions.NotFoundException;
import com.saurabh.personal_finance_manager.repositories.CategoryRepository;
import com.saurabh.personal_finance_manager.repositories.GoalRepository;
import com.saurabh.personal_finance_manager.repositories.TransactionRepository;
import com.saurabh.personal_finance_manager.security.CurrentUserProvider;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

  @Mock private GoalRepository goalRepository;
  @Mock private TransactionRepository transactionRepository;
  @Mock private CurrentUserProvider currentUserProvider;
  @Mock private CategoryRepository categoryRepository;

  @InjectMocks private GoalServiceImpl goalService;

  private User mockUser;

  @BeforeEach
  void setUp() {
    mockUser = new User();
    mockUser.setId(1L);
    mockUser.setUsername("user@example.com");
  }

  @Test
  void testCreate_Success() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    SavingsGoal saved = new SavingsGoal();
    saved.setId(1L);
    saved.setGoalName("New Car");
    saved.setTargetAmount(BigDecimal.valueOf(10000));
    saved.setTargetDate(LocalDate.now().plusMonths(6));
    saved.setStartDate(LocalDate.now());
    saved.setUser(mockUser);

    when(goalRepository.save(any(SavingsGoal.class))).thenReturn(saved);
    when(transactionRepository.findByUserIdAndDeletedFalseAndDateBetween(eq(1L), any(), any()))
        .thenReturn(Collections.emptyList());

    GoalDtos.CreateRequest req =
        new GoalDtos.CreateRequest(
            "New Car", BigDecimal.valueOf(10000), LocalDate.now().plusMonths(6), LocalDate.now());

    GoalDtos.Response res = goalService.create(req);
    assertNotNull(res);
    assertEquals("New Car", res.goalName());
    assertEquals(0.0, res.progressPercentage());
    assertEquals(BigDecimal.valueOf(10000), res.remainingAmount());
  }

  @Test
  void testCreate_InvalidDates_ThrowsBadRequest() {
    GoalDtos.CreateRequest pastTarget =
        new GoalDtos.CreateRequest(
            "Goal", BigDecimal.valueOf(1000), LocalDate.now().minusDays(1), LocalDate.now());
    assertThrows(BadRequestException.class, () -> goalService.create(pastTarget));

    GoalDtos.CreateRequest startAfterTarget =
        new GoalDtos.CreateRequest(
            "Goal", BigDecimal.valueOf(1000), LocalDate.now().plusDays(2), LocalDate.now().plusDays(5));
    assertThrows(BadRequestException.class, () -> goalService.create(startAfterTarget));
  }

  @Test
  void testList_CalculatesProgress() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    Category incomeCat = new Category();
    incomeCat.setId(10L);
    incomeCat.setType(CategoryType.INCOME);

    SavingsGoal g = new SavingsGoal();
    g.setId(1L);
    g.setGoalName("Emergency");
    g.setTargetAmount(BigDecimal.valueOf(10000));
    g.setStartDate(LocalDate.of(2024, 1, 1));
    g.setTargetDate(LocalDate.now().plusYears(1));
    g.setUser(mockUser);
    g.setCategories(List.of(incomeCat));

    when(goalRepository.findByUserIdOrderByIdDesc(1L)).thenReturn(List.of(g));

    Transaction t1 = new Transaction();
    t1.setAmount(BigDecimal.valueOf(6550));
    t1.setCategory(incomeCat);

    when(transactionRepository.findByUserIdAndDeletedFalseAndDateBetween(eq(1L), eq(LocalDate.of(2024, 1, 1)), any()))
        .thenReturn(List.of(t1));

    List<GoalDtos.Response> list = goalService.list();
    assertEquals(1, list.size());
    assertEquals(65.5, list.get(0).progressPercentage());
  }

  @Test
  void testGet_Success() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    SavingsGoal g = new SavingsGoal();
    g.setId(1L);
    g.setGoalName("Emergency");
    g.setTargetAmount(BigDecimal.valueOf(5000));
    g.setStartDate(LocalDate.of(2024, 1, 1));
    g.setTargetDate(LocalDate.now().plusYears(1));
    g.setUser(mockUser);

    when(goalRepository.findById(1L)).thenReturn(Optional.of(g));
    when(transactionRepository.findByUserIdAndDeletedFalseAndDateBetween(eq(1L), any(), any()))
        .thenReturn(Collections.emptyList());

    GoalDtos.Response res = goalService.get(1L);
    assertEquals("Emergency", res.goalName());
  }

  @Test
  void testUpdate_Success() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    SavingsGoal g = new SavingsGoal();
    g.setId(1L);
    g.setGoalName("Emergency");
    g.setTargetAmount(BigDecimal.valueOf(5000));
    g.setStartDate(LocalDate.of(2024, 1, 1));
    g.setTargetDate(LocalDate.now().plusYears(1));
    g.setUser(mockUser);

    when(goalRepository.findById(1L)).thenReturn(Optional.of(g));
    when(goalRepository.save(any(SavingsGoal.class))).thenAnswer(inv -> inv.getArgument(0));
    when(transactionRepository.findByUserIdAndDeletedFalseAndDateBetween(eq(1L), any(), any()))
        .thenReturn(Collections.emptyList());

    GoalDtos.UpdateRequest req =
        new GoalDtos.UpdateRequest(BigDecimal.valueOf(8000), LocalDate.now().plusYears(2));

    GoalDtos.Response res = goalService.update(1L, req);
    assertEquals(BigDecimal.valueOf(8000), res.targetAmount());
  }

  @Test
  void testUpdate_EmptyRequest_ThrowsBadRequest() {
    GoalDtos.UpdateRequest req = new GoalDtos.UpdateRequest(null, null);
    assertThrows(BadRequestException.class, () -> goalService.update(1L, req));
  }

  @Test
  void testDelete_Success() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    SavingsGoal g = new SavingsGoal();
    g.setId(1L);
    g.setUser(mockUser);

    when(goalRepository.findById(1L)).thenReturn(Optional.of(g));

    goalService.delete(1L);
    verify(goalRepository).delete(g);
  }

  @Test
  void testOwned_Forbidden() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    User other = new User();
    other.setId(2L);

    SavingsGoal g = new SavingsGoal();
    g.setId(1L);
    g.setUser(other);

    when(goalRepository.findById(1L)).thenReturn(Optional.of(g));

    assertThrows(ForbiddenException.class, () -> goalService.delete(1L));
  }

  @Test
  void testOwned_NotFound() {
    when(goalRepository.findById(99L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> goalService.delete(99L));
  }

  @Test
  void testProgress_ZeroWhenNoCategoriesAssigned() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    SavingsGoal g = new SavingsGoal();
    g.setId(1L);
    g.setGoalName("Goal without Categories");
    g.setTargetAmount(BigDecimal.valueOf(5000));
    g.setStartDate(LocalDate.of(2024, 1, 1));
    g.setTargetDate(LocalDate.now().plusYears(1));
    g.setUser(mockUser);
    g.setCategories(Collections.emptyList());

    when(goalRepository.findById(1L)).thenReturn(Optional.of(g));

    GoalDtos.Response res = goalService.get(1L);
    assertEquals(BigDecimal.ZERO, res.currentProgress());
    assertEquals(0.0, res.progressPercentage());
    assertEquals(BigDecimal.valueOf(5000), res.remainingAmount());
    assertTrue(res.categoryIds().isEmpty());
  }

  @Test
  void testProgress_CalculatesOnlyForAssignedCategories() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    Category assignedCat = new Category();
    assignedCat.setId(10L);
    assignedCat.setName("Savings Deposit");
    assignedCat.setType(CategoryType.INCOME);

    Category unassignedCat = new Category();
    unassignedCat.setId(20L);
    unassignedCat.setName("Unrelated Bonus");
    unassignedCat.setType(CategoryType.INCOME);

    SavingsGoal g = new SavingsGoal();
    g.setId(1L);
    g.setGoalName("Vacation");
    g.setTargetAmount(BigDecimal.valueOf(1000));
    g.setStartDate(LocalDate.of(2024, 1, 1));
    g.setTargetDate(LocalDate.now().plusYears(1));
    g.setUser(mockUser);
    g.setCategories(List.of(assignedCat));

    when(goalRepository.findById(1L)).thenReturn(Optional.of(g));

    Transaction tAssigned = new Transaction();
    tAssigned.setAmount(BigDecimal.valueOf(300));
    tAssigned.setCategory(assignedCat);

    Transaction tUnassigned = new Transaction();
    tUnassigned.setAmount(BigDecimal.valueOf(500));
    tUnassigned.setCategory(unassignedCat);

    when(transactionRepository.findByUserIdAndDeletedFalseAndDateBetween(eq(1L), eq(LocalDate.of(2024, 1, 1)), any()))
        .thenReturn(List.of(tAssigned, tUnassigned));

    GoalDtos.Response res = goalService.get(1L);
    assertEquals(BigDecimal.valueOf(300), res.currentProgress());
    assertEquals(30.0, res.progressPercentage());
    assertEquals(BigDecimal.valueOf(700), res.remainingAmount());
    assertEquals(List.of(10L), res.categoryIds());
  }

  @Test
  void testCreate_WithCategories_Success() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    Category cat = new Category();
    cat.setId(10L);
    cat.setName("Goal Fund");
    cat.setType(CategoryType.INCOME);
    cat.setCustom(true);
    cat.setUser(mockUser);

    when(categoryRepository.findById(10L)).thenReturn(Optional.of(cat));

    SavingsGoal saved = new SavingsGoal();
    saved.setId(1L);
    saved.setGoalName("House");
    saved.setTargetAmount(BigDecimal.valueOf(50000));
    saved.setTargetDate(LocalDate.now().plusYears(2));
    saved.setStartDate(LocalDate.now());
    saved.setUser(mockUser);

    when(goalRepository.save(any(SavingsGoal.class))).thenReturn(saved);
    when(transactionRepository.findByUserIdAndDeletedFalseAndDateBetween(eq(1L), any(), any()))
        .thenReturn(Collections.emptyList());

    GoalDtos.CreateRequest req =
        new GoalDtos.CreateRequest(
            "House", BigDecimal.valueOf(50000), LocalDate.now().plusYears(2), LocalDate.now(), List.of(10L));

    GoalDtos.Response res = goalService.create(req);
    assertNotNull(res);
    assertEquals("House", res.goalName());
    verify(categoryRepository).save(cat);
    assertEquals(saved, cat.getSavingsGoal());
  }

  @Test
  void testCreate_CategoryAlreadyAssignedToAnotherGoal_ThrowsConflict() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    SavingsGoal otherGoal = new SavingsGoal();
    otherGoal.setId(99L);

    Category cat = new Category();
    cat.setId(10L);
    cat.setName("Already Used");
    cat.setSavingsGoal(otherGoal);

    when(categoryRepository.findById(10L)).thenReturn(Optional.of(cat));

    SavingsGoal saved = new SavingsGoal();
    saved.setId(1L);
    saved.setGoalName("New Goal");
    saved.setTargetAmount(BigDecimal.valueOf(5000));
    saved.setTargetDate(LocalDate.now().plusYears(1));
    saved.setUser(mockUser);

    when(goalRepository.save(any(SavingsGoal.class))).thenReturn(saved);

    GoalDtos.CreateRequest req =
        new GoalDtos.CreateRequest(
            "New Goal", BigDecimal.valueOf(5000), LocalDate.now().plusYears(1), null, List.of(10L));

    assertThrows(ConflictException.class, () -> goalService.create(req));
  }

  @Test
  void testDelete_UnassignsCategories() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    Category cat = new Category();
    cat.setId(10L);
    cat.setName("Fund");

    SavingsGoal g = new SavingsGoal();
    g.setId(1L);
    g.setUser(mockUser);
    cat.setSavingsGoal(g);
    g.setCategories(new java.util.ArrayList<>(List.of(cat)));

    when(goalRepository.findById(1L)).thenReturn(Optional.of(g));

    goalService.delete(1L);

    assertNull(cat.getSavingsGoal());
    verify(categoryRepository).save(cat);
    verify(goalRepository).delete(g);
  }
}

