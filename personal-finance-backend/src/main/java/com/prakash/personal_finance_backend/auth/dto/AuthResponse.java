package com.prakash.personal_finance_backend.auth.dto;

import java.time.Instant;

public record AuthResponse(String tokenType, String accessToken, Instant expiresAt, UserResponse user) {
}