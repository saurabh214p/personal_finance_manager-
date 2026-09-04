package com.saurabh.personal_finance_manager.dtos;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import java.time.Instant;

/** Standard error payload returned by all API error paths. */
public record ErrorResponse(
    Instant timestamp, int status, String error, String message, String path) {}

