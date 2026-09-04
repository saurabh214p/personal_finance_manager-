package com.saurabh.personal_finance_manager.services;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import java.util.List;

/** Contract for category management and category resolution. */
public interface CategoryService {
  CategoryDtos.Response create(CategoryDtos.CreateRequest request);

  List<CategoryDtos.Response> list();

  void delete(String name);

  Category resolveVisible(String name);
}

