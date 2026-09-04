package com.saurabh.personal_finance_manager.config;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Inserts immutable global categories once for a fresh database. */
@Configuration
public class DataSeeder {
  @Bean
  CommandLineRunner seedCategories(CategoryRepository repository) {
    return args -> {
      seed(repository, "Salary", CategoryType.INCOME);
      for (String n :
          new String[] {
            "Food", "Rent", "Transportation", "Entertainment", "Healthcare", "Utilities"
          }) seed(repository, n, CategoryType.EXPENSE);
    };
  }

  private void seed(CategoryRepository r, String name, CategoryType type) {
    if (r.findByNameIgnoreCaseAndCustomFalse(name).isEmpty()) {
      Category c = new Category();
      c.setName(name);
      c.setType(type);
      c.setCustom(false);
      r.save(c);
    }
  }
}

