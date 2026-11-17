package com.prakash.personal_finance_backend.security;

/**
 * Thrown for any malformed, unsigned, tampered, or expired token. Callers
 * treat this the same way regardless of which of those it was — the JWT
 * filter just drops the auth attempt and lets the request fall through as
 * unauthenticated.
 */
public class JwtValidationException extends RuntimeException {

    public JwtValidationException(String message) {
        super(message);
    }
}