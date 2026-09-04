package com.saurabh.personal_finance_manager.services;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/** Implements registration rules. */
@Service
public class AuthServiceImpl implements AuthService {
  private final UserRepository users;
  private final PasswordEncoder encoder;

  public AuthServiceImpl(UserRepository users, PasswordEncoder encoder) {
    this.users = users;
    this.encoder = encoder;
  }

  public AuthDtos.RegisterResponse register(AuthDtos.RegisterRequest r) {
    if (users.existsByUsername(r.username()))
      throw new ConflictException("Username is already registered");
    User u = new User();
    u.setUsername(r.username().trim().toLowerCase());
    u.setPassword(encoder.encode(r.password()));
    u.setFullName(r.fullName().trim());
    u.setPhoneNumber(r.phoneNumber().trim());
    u = users.save(u);
    return new AuthDtos.RegisterResponse("User registered successfully", u.getId());
  }
}

