package com.saurabh.personal_finance_manager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.saurabh.personal_finance_manager.dtos.CategoryDtos;
import com.saurabh.personal_finance_manager.entities.Category;
import com.saurabh.personal_finance_manager.entities.CategoryType;
import com.saurabh.personal_finance_manager.entities.User;
import com.saurabh.personal_finance_manager.exceptions.BadRequestException;
import com.saurabh.personal_finance_manager.exceptions.ConflictException;
import com.saurabh.personal_finance_manager.exceptions.ForbiddenException;
import com.saurabh.personal_finance_manager.exceptions.NotFoundException;
import com.saurabh.personal_finance_manager.repositories.CategoryRepository;
import com.saurabh.personal_finance_manager.repositories.TransactionRepository;
import com.saurabh.personal_finance_manager.security.CurrentUserProvider;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

  @Mock private CategoryRepository categoryRepository;
  @Mock private TransactionRepository transactionRepository;
  @Mock private CurrentUserProvider currentUserProvider;

  @InjectMocks private CategoryServiceImpl categoryService;

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
    when(categoryRepository.existsByNameIgnoreCaseAndUserId("Bonus", 1L)).thenReturn(false);

    Category savedCategory = new Category();
    savedCategory.setId(10L);
    savedCategory.setName("Bonus");
    savedCategory.setType(CategoryType.INCOME);
    savedCategory.setCustom(true);
    savedCategory.setUser(mockUser);

    when(categoryRepository.save(any(Category.class))).thenReturn(savedCategory);

    CategoryDtos.CreateRequest request = new CategoryDtos.CreateRequest("Bonus", CategoryType.INCOME);
    CategoryDtos.Response response = categoryService.create(request);

    assertNotNull(response);
    assertEquals("Bonus", response.name());
    assertEquals(CategoryType.INCOME, response.type());
    assertTrue(response.isCustom());
    assertTrue(response.custom());
  }

  @Test
  void testCreate_DuplicateName_ThrowsConflictException() {
    when(currentUserProvider.current()).thenReturn(mockUser);
    when(categoryRepository.existsByNameIgnoreCaseAndUserId("Bonus", 1L)).thenReturn(true);

    CategoryDtos.CreateRequest request = new CategoryDtos.CreateRequest("Bonus", CategoryType.INCOME);
    assertThrows(ConflictException.class, () -> categoryService.create(request));
  }

  @Test
  void testList_ReturnsCategories() {
    when(currentUserProvider.current()).thenReturn(mockUser);

    Category c1 = new Category();
    c1.setId(1L);
    c1.setName("Salary");
    c1.setType(CategoryType.INCOME);
    c1.setCustom(false);

    when(categoryRepository.findByCustomFalseOrUserIdOrderByNameAsc(1L)).thenReturn(List.of(c1));

    List<CategoryDtos.Response> list = categoryService.list();
    assertEquals(1, list.size());
    assertEquals("Salary", list.get(0).name());
  }

  @Test
  void testDelete_DefaultCategory_ThrowsForbiddenException() {
    when(currentUserProvider.current()).thenReturn(mockUser);
    Category defaultCategory = new Category();
    defaultCategory.setId(1L);
    defaultCategory.setName("Salary");
    defaultCategory.setCustom(false);

    when(categoryRepository.findByNameIgnoreCaseAndCustomFalse("Salary")).thenReturn(Optional.of(defaultCategory));

    assertThrows(ForbiddenException.class, () -> categoryService.delete("Salary"));
  }

  @Test
  void testDelete_CategoryInUse_ThrowsConflictException() {
    when(currentUserProvider.current()).thenReturn(mockUser);
    Category customCategory = new Category();
    customCategory.setId(2L);
    customCategory.setName("Freelance");
    customCategory.setCustom(true);
    customCategory.setUser(mockUser);

    when(categoryRepository.findByNameIgnoreCaseAndCustomFalse("Freelance")).thenReturn(Optional.empty());
    when(categoryRepository.findByNameIgnoreCaseAndUserId("Freelance", 1L)).thenReturn(Optional.of(customCategory));
    when(transactionRepository.existsByCategoryIdAndDeletedFalse(2L)).thenReturn(true);

    assertThrows(ConflictException.class, () -> categoryService.delete("Freelance"));
  }

  @Test
  void testDelete_Success() {
    when(currentUserProvider.current()).thenReturn(mockUser);
    Category customCategory = new Category();
    customCategory.setId(2L);
    customCategory.setName("Freelance");
    customCategory.setCustom(true);
    customCategory.setUser(mockUser);

    when(categoryRepository.findByNameIgnoreCaseAndCustomFalse("Freelance")).thenReturn(Optional.empty());
    when(categoryRepository.findByNameIgnoreCaseAndUserId("Freelance", 1L)).thenReturn(Optional.of(customCategory));
    when(transactionRepository.existsByCategoryIdAndDeletedFalse(2L)).thenReturn(false);

    assertDoesNotThrow(() -> categoryService.delete("Freelance"));
    verify(categoryRepository).delete(customCategory);
  }

  @Test
  void testDelete_NotFound_ThrowsNotFoundException() {
    when(currentUserProvider.current()).thenReturn(mockUser);
    when(categoryRepository.findByNameIgnoreCaseAndCustomFalse("Unknown")).thenReturn(Optional.empty());
    when(categoryRepository.findByNameIgnoreCaseAndUserId("Unknown", 1L)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> categoryService.delete("Unknown"));
  }

  @Test
  void testResolveVisible_Found() {
    when(currentUserProvider.current()).thenReturn(mockUser);
    Category c = new Category();
    c.setId(1L);
    c.setName("Salary");

    when(categoryRepository.findByNameIgnoreCaseAndCustomFalse("Salary")).thenReturn(Optional.of(c));

    Category resolved = categoryService.resolveVisible("Salary");
    assertEquals("Salary", resolved.getName());
  }

  @Test
  void testResolveVisible_NotFound_ThrowsBadRequest() {
    when(currentUserProvider.current()).thenReturn(mockUser);
    when(categoryRepository.findByNameIgnoreCaseAndCustomFalse("Unknown")).thenReturn(Optional.empty());
    when(categoryRepository.findByNameIgnoreCaseAndUserId("Unknown", 1L)).thenReturn(Optional.empty());

    assertThrows(BadRequestException.class, () -> categoryService.resolveVisible("Unknown"));
  }
}
