package com.saurabh.personal_finance_manager.exceptions;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import org.springframework.http.HttpStatus;

/** Thrown for an authenticated caller without ownership or permission. */
public class ForbiddenException extends ApiException {
  public ForbiddenException(String message) {
    super(HttpStatus.FORBIDDEN, message);
  }
}

