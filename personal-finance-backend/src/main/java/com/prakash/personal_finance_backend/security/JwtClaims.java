package com.prakash.personal_finance_backend.security;

/**
 * Claims extracted from a validated JWT — just enough to identify the caller
 * without a DB round trip for every request.
 */
public record JwtClaims(Long userId, String email) {
}