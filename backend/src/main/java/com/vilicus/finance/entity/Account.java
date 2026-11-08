package com.vilicus.finance.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Account entity representing a financial account (bank, credit card, etc.)
 *
 * Attributes:
 * - userId: Foreign key to User (owner of account)
 * - iban: International Bank Account Number (unique per user)
 * - name: User-friendly account name (e.g., "Checking", "Savings")
 * - type: Account type (bank, credit card, cash, savings, investment)
 * - currency: ISO 4217 currency code (EUR, USD, GBP, etc.)
 * - balance: Current account balance in currency
 * - status: active, archived, or closed
 * - createdAt: Account creation timestamp
 * - updatedAt: Last modification timestamp
 *
 * Relationships:
 * - User (owner): Many-to-One relationship
 *
 * Constraints:
 * - UNIQUE (user_id, iban): User cannot have duplicate IBANs
 * - UNIQUE (user_id, name): User cannot have duplicate account names
 * - balance default: 0.00
 * - status default: 'active'
 * - currency default: 'EUR'
 */
@Entity
@Table(name = "accounts",
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "iban"}, name = "uc_accounts_user_iban"),
           @UniqueConstraint(columnNames = {"user_id", "name"}, name = "uc_accounts_user_name")
       },
       indexes = {
           @Index(name = "idx_accounts_user_id", columnList = "user_id"),
           @Index(name = "idx_accounts_status", columnList = "status"),
           @Index(name = "idx_accounts_type", columnList = "type")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "User ID is required")
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @NotBlank(message = "IBAN is required")
    @Column(nullable = false, length = 34)
    private String iban;

    @NotBlank(message = "Account name is required")
    @Column(nullable = false, length = 100)
    private String name;

    @NotNull(message = "Account type is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountType type;

    @NotBlank(message = "Currency is required")
    @Column(nullable = false, length = 3)
    private String currency;

    @NotNull(message = "Balance is required")
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal balance;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "active";

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle hook: set createdAt and updatedAt on persist
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    /**
     * JPA lifecycle hook: update updatedAt on every update
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Validate IBAN format (simplified regex for common IBAN patterns)
     * Full IBAN validation: https://en.wikipedia.org/wiki/International_Bank_Account_Number
     *
     * @return true if IBAN matches expected format
     */
    public boolean isValidIban() {
        if (iban == null || iban.length() < 15 || iban.length() > 34) {
            return false;
        }
        // IBAN format: 2 letters (country) + 2 digits (check) + up to 30 alphanumeric
        return iban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$");
    }

    /**
     * Check if account is active
     */
    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    /**
     * Archive account (soft delete)
     */
    public void archive() {
        this.status = "archived";
        this.updatedAt = LocalDateTime.now();
    }
}
