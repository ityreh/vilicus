package com.vilicus.finance.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * UpdateTransactionCategoryRequest — Request DTO for category update.
 *
 * Used by:
 * - PUT /api/transactions/{id}/category (update single)
 * - POST /api/transactions/recategorize (bulk update)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTransactionCategoryRequest {

    /**
     * Category ID to assign to transaction(s).
     * Must be a valid category ID from the categories table.
     */
    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
