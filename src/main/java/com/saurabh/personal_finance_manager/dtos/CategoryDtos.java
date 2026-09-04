package com.saurabh.personal_finance_manager.dtos;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.entities.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import java.util.List;

/** Category request and response DTOs. */
public final class CategoryDtos {
  private CategoryDtos() {}

  public record CreateRequest(@NotBlank String name, @NotNull CategoryType type) {}

  public record Response(
      Long id,
      String name,
      CategoryType type,
      @JsonProperty("isCustom") boolean isCustom) {

    @JsonProperty("custom")
    public boolean custom() {
      return isCustom;
    }
  }

  public record ListResponse(List<Response> categories) {}
}

