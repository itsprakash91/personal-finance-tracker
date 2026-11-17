package com.prakash.personal_finance_backend.repository;

import com.prakash.personal_finance_backend.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    Optional<Budget> findByIdAndUser_Id(Long id, Long userId);

    // All budgets a user set for a given month — feeds the spend-vs-budget
    // dashboard summary.
    List<Budget> findByUser_IdAndMonth(Long userId, LocalDate month);

    Optional<Budget> findByUser_IdAndCategory_IdAndMonth(Long userId, Long categoryId, LocalDate month);
}