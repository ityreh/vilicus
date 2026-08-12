package com.vilicus.finance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * BulkRecategorizeResponseDto — Response DTO for bulk categorization.
 *
 * Returned by: POST /api/transactions/recategorize
 *
 * Shows how many transactions were successfully categorized.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkRecategorizeResponseDto {

    /**
     * Number of transactions that were successfully categorized.
     */
    private int updatedCount;

    /**
     * Category ID that was assigned.
     */
    private Long categoryId;

    /**
     * Human-readable result message.
     * Example: "Successfully categorized 25 transactions"
     */
    private String message;
}
