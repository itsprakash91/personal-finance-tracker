package com.prakash.personal_finance_backend.repository;

import com.prakash.personal_finance_backend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByUser_Id(Long userId);

    // Ownership-scoped lookup: use this (never findById alone) wherever a user
    // acts on one account, so a stranger's account id 404s instead of 200-ing.
    Optional<Account> findByIdAndUser_Id(Long id, Long userId);
}