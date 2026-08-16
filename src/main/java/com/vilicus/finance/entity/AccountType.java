package com.vilicus.finance.entity;

/**
 * Account type enumeration for categorizing financial accounts.
 *
 * Types:
 * - BANK_ACCOUNT: Standard checking or savings accounts
 * - CREDIT_CARD: Credit card with credit line
 * - CASH: Physical cash tracking
 * - SAVINGS: Long-term savings accounts
 * - INVESTMENT: Brokerage or investment accounts
 */
public enum AccountType {
    BANK_ACCOUNT("Bank Account"),
    CREDIT_CARD("Credit Card"),
    CASH("Cash"),
    SAVINGS("Savings Account"),
    INVESTMENT("Investment Account");

    private final String displayName;

    AccountType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
