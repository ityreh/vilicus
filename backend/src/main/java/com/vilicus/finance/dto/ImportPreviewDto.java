package com.vilicus.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * ImportPreviewDto — Preview of transactions before import is confirmed.
 *
 * Returned by POST /api/accounts/{id}/import/preview endpoint.
 * User reviews this before confirming the import with POST /api/accounts/{id}/import/confirm.
 *
 * Contains: transaction count, warnings, duplicates, balance info.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportPreviewDto {

    /**
     * Import session ID (used to store uploaded file in memory/cache).
     * Client includes this in the confirm request to identify which upload to process.
     */
    private String importId;

    /**
     * File name that was uploaded.
     * Used for display/logging.
     */
    private String fileName;

    /**
     * File format detected: "CAMT.052", "CSV", "OFX".
     */
    private String fileFormat;

    /**
     * IBAN extracted from the statement file.
     * Used to match against the account being imported to.
     */
    private String iban;

    /**
     * Opening balance from the statement (if provided).
     * Helps user verify statement integrity.
     */
    private BigDecimal openingBalance;

    /**
     * Closing balance from the statement (if provided).
     * User can verify this matches their bank.
     */
    private BigDecimal closingBalance;

    /**
     * Statement date range: from date.
     */
    private LocalDate statementDateFrom;

    /**
     * Statement date range: to date.
     */
    private LocalDate statementDateTo;

    /**
     * Count of transactions that will be imported.
     * New transactions (not duplicates).
     */
    private int newTransactionCount;

    /**
     * Count of transactions detected as duplicates.
     * (Already in database, by txId.)
     * These will be skipped during import.
     */
    private int duplicateTransactionCount;

    /**
     * Total transactions in file.
     * = newTransactionCount + duplicateTransactionCount
     */
    private int totalTransactionCount;

    /**
     * Preview of transactions to be imported.
     * Shows first 10 transactions (sample).
     * User can review structure before confirming.
     */
    private List<TransactionPreviewDto> transactionSample;

    /**
     * Validation errors and warnings.
     * If not empty, user should review before confirming.
     *
     * Examples:
     * - "IBAN mismatch: file has DE89..., account is FR14..."
     * - "3 transactions skipped (malformed)"
     * - "Balance mismatch: expected 1000.00, calculated 999.99"
     */
    private List<String> warnings;

    /**
     * Whether import is safe to proceed.
     * True if no critical errors (some warnings are OK).
     * False if critical issues (IBAN mismatch, parsing errors, etc.)
     */
    private boolean canProceed;

    /**
     * Simple transaction preview (for the sample list).
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class TransactionPreviewDto {
        private LocalDate txDate;
        private BigDecimal amount;
        private String direction; // DEBIT or CREDIT
        private String description;
        private String counterparty;
        private String status; // "new" or "duplicate"
    }
}
