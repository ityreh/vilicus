package com.vilicus.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * TransactionDto — Data Transfer Object for Transaction responses.
 *
 * Used for: API responses, serialization to JSON.
 * Includes all relevant fields for viewing/filtering transactions.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDto {

    private Long id;
    private Long accountId;
    private String txId;
    private LocalDate txDate;
    private BigDecimal amount;
    private BigDecimal balance;
    private String direction; // "DEBIT" or "CREDIT"
    private String description;
    private String counterparty;
    private String reference;
    private Long categoryId;
    private String status; // "imported", "categorized", "archived"
    private String notes;
    private String importSource; // "CAMT.052", "CSV", "OFX"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
