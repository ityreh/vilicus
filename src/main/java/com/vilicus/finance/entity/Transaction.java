package com.vilicus.finance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Transaction entity representing a single bank transaction.
 *
 * Used for storing imported transactions from bank statements (CAMT.052, CSV, OFX).
 * Fields are normalized to support multiple import formats.
 *
 * Deduplication: Unique constraint on (account_id, tx_id) ensures no exact duplicates.
 * The tx_id field comes from the bank (SWIFT transaction ID in CAMT.052).
 */
@Entity
@Table(name = "transactions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"account_id", "tx_id"}, name = "uc_transactions_account_tx_id")
        },
        indexes = {
                @Index(name = "idx_transactions_account_id", columnList = "account_id"),
                @Index(name = "idx_transactions_category_id", columnList = "category_id"),
                @Index(name = "idx_transactions_date", columnList = "tx_date"),
                @Index(name = "idx_transactions_counterparty", columnList = "counterparty"),
                @Index(name = "idx_transactions_account_date", columnList = "account_id, tx_date")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Account this transaction belongs to.
     * Foreign key to accounts table.
     * Ensures all transactions are scoped to an account (and thus to a user).
     */
    @Column(name = "account_id", nullable = false)
    private Long accountId;

    /**
     * Transaction ID from the bank (e.g., SWIFT transaction ID in CAMT.052).
     * Unique per account — banks guarantee uniqueness within a statement.
     * Used for deduplication: if tx_id already exists for this account, skip.
     *
     * Format varies by bank/source:
     * - CAMT.052: Proprietary transaction ID from bank
     * - CSV: Generated hash or reference field
     * - OFX: FITID from statement
     *
     * Max 50 chars to accommodate various formats.
     */
    @Column(name = "tx_id", nullable = false, length = 50)
    private String txId;

    /**
     * Transaction date (when the transaction was booked).
     * ISO format: YYYY-MM-DD
     * Used for sorting, filtering, and date-range queries.
     */
    @Column(name = "tx_date", nullable = false)
    private LocalDate txDate;

    /**
     * Transaction amount (always positive).
     * Sign (debit/credit) is stored in direction field.
     * Precision: 19 digits, 2 decimal places (EUR standard).
     */
    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /**
     * Running account balance after this transaction.
     * Used to verify statement consistency.
     * Set during import from CAMT opening/closing balances.
     * Nullable for CSV imports (where balance might not be available).
     */
    @Column(name = "balance", precision = 19, scale = 2)
    private BigDecimal balance;

    /**
     * Transaction direction: DEBIT or CREDIT.
     * Indicates whether amount was withdrawn (DEBIT) or deposited (CREDIT).
     */
    @Column(name = "direction", nullable = false, length = 10)
    private String direction; // "DEBIT" or "CREDIT"

    /**
     * Description of the transaction.
     * Usually the payment purpose or memo from the bank statement.
     *
     * Examples:
     * - "Invoice #12345 — Supplier Inc."
     * - "MIETE Wohnung — Hausmeister"
     * - "Gehalt August 2026"
     *
     * Max 500 chars to accommodate detailed descriptions.
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * Counterparty name (who sent/received the money).
     * The other party in the transaction.
     *
     * Examples:
     * - Creditor name (money came from)
     * - Debtor name (money went to)
     * - Vendor/supplier name
     *
     * Used for categorization rules ("Rewe" → Groceries, etc.)
     * Max 100 chars.
     */
    @Column(name = "counterparty", length = 100)
    private String counterparty;

    /**
     * Reference/booking text from the bank.
     * Additional metadata from the statement (e.g., invoice number, reference ID).
     *
     * In CAMT.052: Often from RemittanceInformation field.
     * In CSV: Usually the reference column.
     *
     * Max 200 chars.
     */
    @Column(name = "reference", length = 200)
    private String reference;

    /**
     * Category ID for this transaction.
     * Foreign key to categories table (future Phase 4).
     * Nullable initially; assigned during import or by user action.
     *
     * Used for: spending breakdown, budget tracking, analytics.
     */
    @Column(name = "category_id")
    private Long categoryId;

    /**
     * Status of this transaction.
     * - "imported": Just imported from statement
     * - "categorized": User has assigned a category
     * - "archived": Hidden from view (soft delete)
     *
     * Default: "imported"
     */
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private String status = "imported";

    /**
     * Notes added by user (optional).
     * User can add comments or metadata to a transaction.
     * Max 500 chars.
     */
    @Column(name = "notes", length = 500)
    private String notes;

    /**
     * Import source (metadata).
     * Where did this transaction come from?
     * - "CAMT.052"
     * - "CSV"
     * - "OFX"
     *
     * Used for debugging and audit trail.
     */
    @Column(name = "import_source", length = 50)
    private String importSource;

    /**
     * Timestamp when this record was created (first imported).
     * Set automatically via @PrePersist.
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * Timestamp when this record was last modified.
     * Updated automatically via @PreUpdate.
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * JPA lifecycle hook: set createdAt and updatedAt before first insert.
     */
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = "imported";
        }
    }

    /**
     * JPA lifecycle hook: update updatedAt before every update.
     */
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    /**
     * Check if transaction is marked as archived.
     * @return true if status is "archived"
     */
    public boolean isArchived() {
        return "archived".equalsIgnoreCase(status);
    }

    /**
     * Archive this transaction (soft delete).
     * Changes status to "archived" and updates timestamp.
     */
    public void archive() {
        this.status = "archived";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Mark transaction as categorized.
     * Changes status to "categorized" and updates timestamp.
     * @param categoryId the category ID assigned
     */
    public void categorize(Long categoryId) {
        this.categoryId = categoryId;
        this.status = "categorized";
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Verify transaction has required fields for import.
     * Called before saving to database.
     *
     * @return true if all required fields are present
     */
    public boolean isValid() {
        return accountId != null
                && txId != null && !txId.isBlank()
                && txDate != null
                && amount != null && amount.signum() >= 0
                && direction != null && (direction.equals("DEBIT") || direction.equals("CREDIT"))
                && description != null && !description.isBlank();
    }
}
