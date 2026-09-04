package com.saurabh.personal_finance_manager.entities;

import com.saurabh.personal_finance_manager.controllers.*;
import com.saurabh.personal_finance_manager.dtos.*;
import com.saurabh.personal_finance_manager.exceptions.*;
import com.saurabh.personal_finance_manager.mappers.*;
import com.saurabh.personal_finance_manager.repositories.*;
import com.saurabh.personal_finance_manager.security.*;
import com.saurabh.personal_finance_manager.services.*;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/** User savings target whose progress is computed from transactions. */
@Entity
@Getter
@Setter
public class SavingsGoal {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String goalName;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal targetAmount;

  @Column(nullable = false)
  private LocalDate targetDate;

  @Column(nullable = false)
  private LocalDate startDate;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(nullable = false)
  private User user;
}

