package com.prakash.personal_finance_backend.auth.dto;

import com.prakash.personal_finance_backend.entity.User;

/**
 * Never return the User entity directly from a controller — it carries
 * passwordHash. This is the only shape of a user that ever leaves the API.
 */
public record UserResponse(Long id, String email, String displayName, String defaultCurrency) {

    public static UserResponse fromEntity(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getDefaultCurrency());
    }
}