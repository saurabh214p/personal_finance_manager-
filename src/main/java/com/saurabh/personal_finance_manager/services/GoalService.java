package com.saurabh.personal_finance_manager.services;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import java.util.List;

/** Contract for savings-goal management. */
public interface GoalService {
  GoalDtos.Response create(GoalDtos.CreateRequest request);

  List<GoalDtos.Response> list();

  GoalDtos.Response get(Long id);

  GoalDtos.Response update(Long id, GoalDtos.UpdateRequest request);

  void delete(Long id);
}

