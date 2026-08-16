package com.vilicus.finance.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * BulkRecategorizeRequest — Request DTO for bulk transaction categorization.
 *
 * Used by: POST /api/transactions/recategorize
 *
 * Allows categorizing multiple transactions at once.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkRecategorizeRequest {

    /**
     * List of transaction IDs to categorize.
     * Must be non-empty.
     */
    @NotEmpty(message = "Transaction IDs list cannot be empty")
    private List<Long> transactionIds;

    /**
     * Category ID to assign to all transactions.
     * Must be a valid category ID.
     */
    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
