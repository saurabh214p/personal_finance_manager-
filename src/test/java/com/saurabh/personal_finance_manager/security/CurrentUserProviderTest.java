package com.saurabh.personal_finance_manager.security;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.saurabh.personal_finance_manager.entities.User;
import com.saurabh.personal_finance_manager.exceptions.NotFoundException;
import com.saurabh.personal_finance_manager.repositories.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CurrentUserProviderTest {

  @Mock private UserRepository userRepository;
  @InjectMocks private CurrentUserProvider currentUserProvider;

  @AfterEach
  void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  void testCurrent_Success() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("john@example.com", "pass"));

    User user = new User();
    user.setUsername("john@example.com");

    when(userRepository.findByUsername("john@example.com")).thenReturn(Optional.of(user));

    User current = currentUserProvider.current();
    assertNotNull(current);
    assertEquals("john@example.com", current.getUsername());
  }

  @Test
  void testCurrent_NotFound() {
    SecurityContextHolder.getContext()
        .setAuthentication(new UsernamePasswordAuthenticationToken("unknown@example.com", "pass"));

    when(userRepository.findByUsername("unknown@example.com")).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> currentUserProvider.current());
  }
}
