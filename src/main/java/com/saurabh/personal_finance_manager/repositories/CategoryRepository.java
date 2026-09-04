package com.saurabh.personal_finance_manager.repositories;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;

/** Persistence access for categories. */
public interface CategoryRepository extends JpaRepository<Category, Long> {
  List<Category> findByCustomFalseOrUserIdOrderByNameAsc(Long userId);

  Optional<Category> findByNameIgnoreCaseAndCustomFalse(String name);

  Optional<Category> findByNameIgnoreCaseAndUserId(String name, Long userId);

  boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);
}

