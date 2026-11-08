package com.vilicus.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * ImportResultDto — Result of a confirmed transaction import.
 *
 * Returned by POST /api/accounts/{id}/import/confirm endpoint.
 * Shows how many transactions were imported, how many were skipped, and the result message.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImportResultDto {

    /**
     * Number of transactions that were successfully imported.
     */
    private int importedCount;

    /**
     * Number of duplicate transactions that were skipped.
     */
    private int duplicateSkipped;

    /**
     * Statement date range: from date.
     */
    private LocalDate statementDateFrom;

    /**
     * Statement date range: to date.
     */
    private LocalDate statementDateTo;

    /**
     * Human-readable result message.
     * Example: "Successfully imported 150 transactions"
     */
    private String message;
}
