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

/** Persistence access for savings goals. */
public interface GoalRepository extends JpaRepository<SavingsGoal, Long> {
  List<SavingsGoal> findByUserIdOrderByIdDesc(Long userId);
}

