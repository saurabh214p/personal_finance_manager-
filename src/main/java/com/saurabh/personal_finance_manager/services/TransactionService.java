package com.saurabh.personal_finance_manager.services;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import java.time.LocalDate;
import java.util.List;

/** Contract for transaction CRUD and filtered listing. */
public interface TransactionService {
  TransactionDtos.Response create(TransactionDtos.CreateRequest request);

  List<TransactionDtos.Response> list(
      LocalDate startDate, LocalDate endDate, Long categoryId, CategoryType type);

  List<TransactionDtos.Response> list(
      LocalDate startDate, LocalDate endDate, Long categoryId, String category, CategoryType type);

  TransactionDtos.Response update(Long id, TransactionDtos.UpdateRequest request);

  void delete(Long id);
}

