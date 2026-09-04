package com.saurabh.personal_finance_manager.mappers;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;

/** Translates transaction entities to API response DTOs. */
public final class TransactionMapper {
  private TransactionMapper() {}

  public static TransactionDtos.Response response(Transaction t) {
    return new TransactionDtos.Response(
        t.getId(),
        t.getAmount(),
        t.getDate(),
        t.getCategory().getName(),
        t.getDescription(),
        t.getCategory().getType());
  }

  public static void apply(Transaction t, TransactionDtos.CreateRequest r, Category c) {
    t.setAmount(r.amount());
    t.setDate(r.date());
    t.setCategory(c);
    t.setDescription(r.description());
  }
}

