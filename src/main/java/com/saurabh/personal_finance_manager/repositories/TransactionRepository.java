package com.saurabh.personal_finance_manager.repositories;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import java.time.LocalDate;
import java.util.*;
import org.springframework.data.jpa.repository.*;

/** Persistence access for financial entries. */
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
  List<Transaction> findByUserIdAndDeletedFalseOrderByDateDescIdDesc(Long userId);

  List<Transaction> findByUserIdAndDeletedFalseAndDateBetween(
      Long userId, LocalDate start, LocalDate end);

  boolean existsByCategoryIdAndDeletedFalse(Long categoryId);
}

