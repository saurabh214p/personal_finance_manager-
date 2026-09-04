package com.saurabh.personal_finance_manager.security;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.services.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/** Resolves the authenticated persisted user, never from request input. */
@Component
public class CurrentUserProvider {
  private final UserRepository users;

  public CurrentUserProvider(UserRepository users) {
    this.users = users;
  }

  public User current() {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return users
        .findByUsername(username)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }
}

