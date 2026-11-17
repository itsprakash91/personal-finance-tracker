package com.prakash.personal_finance_backend.security;

/**
 * The authenticated caller, as carried in the SecurityContext for the
 * lifetime of one request. Controllers pull this via
 * {@code @AuthenticationPrincipal UserPrincipal principal} to scope every
 * query to principal.id() — this is the hook every later feature
 * (Account/Category/Transaction/Budget) uses to enforce "user can only touch
 * their own data".
 */
public record UserPrincipal(Long id, String email) {
}