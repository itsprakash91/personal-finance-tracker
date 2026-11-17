package com.prakash.personal_finance_backend.repository;

import com.prakash.personal_finance_backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Ownership-scoped lookup: account.user.id, not just account_id, so a
    // transaction can only be read/edited/deleted by the account's owner.
    Optional<Transaction> findByIdAndAccount_User_Id(Long id, Long userId);

    // Filtering by account/category/date range + pagination is built out with
    // the Transaction CRUD feature (Specifications/JPQL); this repo currently
    // only has what the entity skeleton needs.
}