package com.prakash.personal_finance_backend.entity;

/**
 * Type of "bucket" money sits in. Mirrors the CHECK constraint on accounts.type
 * in V1__init_schema.sql.
 */
public enum AccountType {
    CHECKING,
    SAVINGS,
    CREDIT_CARD,
    CASH
}