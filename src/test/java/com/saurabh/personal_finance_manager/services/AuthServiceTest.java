package com.saurabh.personal_finance_manager.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.saurabh.personal_finance_manager.dtos.AuthDtos;
import com.saurabh.personal_finance_manager.entities.User;
import com.saurabh.personal_finance_manager.exceptions.ConflictException;
import com.saurabh.personal_finance_manager.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordEncoder passwordEncoder;

  @InjectMocks private AuthServiceImpl authService;

  @Test
  void testRegister_Success() {
    AuthDtos.RegisterRequest request =
        new AuthDtos.RegisterRequest("user@example.com", "password123", "John Doe", "+1234567890");

    when(userRepository.existsByUsername("user@example.com")).thenReturn(false);
    when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

    User savedUser = new User();
    savedUser.setId(1L);
    savedUser.setUsername("user@example.com");
    savedUser.setFullName("John Doe");
    savedUser.setPhoneNumber("+1234567890");
    savedUser.setPassword("encodedPassword");

    when(userRepository.save(any(User.class))).thenReturn(savedUser);

    AuthDtos.RegisterResponse response = authService.register(request);

    assertNotNull(response);
    assertEquals(1L, response.userId());
    assertEquals("User registered successfully", response.message());
    verify(userRepository).save(any(User.class));
  }

  @Test
  void testRegister_DuplicateUsername_ThrowsConflictException() {
    AuthDtos.RegisterRequest request =
        new AuthDtos.RegisterRequest("user@example.com", "password123", "John Doe", "+1234567890");

    when(userRepository.existsByUsername("user@example.com")).thenReturn(true);

    assertThrows(ConflictException.class, () -> authService.register(request));
    verify(userRepository, never()).save(any(User.class));
  }
}
