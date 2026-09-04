package com.saurabh.personal_finance_manager.exceptions;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import org.springframework.http.HttpStatus;

/** Thrown when a request violates a business validation rule. */
public class BadRequestException extends ApiException {
  public BadRequestException(String message) {
    super(HttpStatus.BAD_REQUEST, message);
  }
}

