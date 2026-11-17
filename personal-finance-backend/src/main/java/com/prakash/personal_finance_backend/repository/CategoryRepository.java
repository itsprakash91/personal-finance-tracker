package com.prakash.personal_finance_backend.repository;

import com.prakash.personal_finance_backend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // System defaults (user IS NULL) + this user's own custom categories.
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user.id = :userId")
    List<Category> findVisibleToUser(@Param("userId") Long userId);

    // Ownership-scoped lookup for edit/delete: a category with user IS NULL
    // (system default) never matches here, so it can't be mutated by a user.
    Optional<Category> findByIdAndUser_Id(Long id, Long userId);

    List<Category> findByUserIsNull();
}