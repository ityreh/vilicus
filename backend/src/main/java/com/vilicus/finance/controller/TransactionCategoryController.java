package com.vilicus.finance.controller;

import com.vilicus.finance.dto.BulkRecategorizeRequest;
import com.vilicus.finance.dto.BulkRecategorizeResponseDto;
import com.vilicus.finance.dto.TransactionDto;
import com.vilicus.finance.dto.UpdateTransactionCategoryRequest;
import com.vilicus.finance.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for transaction category management.
 *
 * All endpoints require JWT authentication.
 * Paths: /api/transactions/*
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionCategoryController {

    private final TransactionService transactionService;

    /**
     * Update category for a single transaction.
     *
     * PUT /api/transactions/{id}/category
     *
     * Mutable fields: categoryId (status automatically changes to "categorized")
     * Immutable fields: txId, txDate, amount, direction, description, counterparty
     *
     * Request body:
     * {
     *   "categoryId": 5
     * }
     *
     * Response (200):
     * {
     *   "id": 1,
     *   "accountId": 1,
     *   "txId": "TXN-001",
     *   "txDate": "2026-08-10",
     *   "amount": 100.50,
     *   "direction": "DEBIT",
     *   "description": "Invoice Payment",
     *   "counterparty": "Supplier A",
     *   "categoryId": 5,
     *   "status": "categorized",
     *   ...
     * }
     *
     * @param authentication Spring Security authentication (JWT)
     * @param accountId account ID (for verification)
     * @param id transaction ID to categorize
     * @param request update request with categoryId
     * @return updated transaction (200 OK) or 404/400 on error
     * @throws ResourceNotFoundException if transaction not found
     */
    @PutMapping("/{id}/category")
    public ResponseEntity<TransactionDto> updateCategory(
            Authentication authentication,
            @RequestParam Long accountId,
            @PathVariable Long id,
            @Valid @RequestBody UpdateTransactionCategoryRequest request) {

        log.info("PUT /api/transactions/{}/category - User: {}, AccountId: {}, CategoryId: {}",
                id, authentication.getName(), accountId, request.getCategoryId());

        Long userId = extractUserIdFromAuthentication(authentication);

        TransactionDto updatedTransaction = transactionService.updateTransactionCategory(
                accountId, id, request.getCategoryId());

        log.info("Transaction {} categorized as {} - status changed to 'categorized'",
                id, request.getCategoryId());

        return ResponseEntity.ok(updatedTransaction);
    }

    /**
     * Bulk update categories for multiple transactions.
     *
     * POST /api/transactions/recategorize
     *
     * Request body:
     * {
     *   "transactionIds": [1, 2, 3, 4, 5],
     *   "categoryId": 5
     * }
     *
     * Response (200):
     * {
     *   "updatedCount": 5,
     *   "categoryId": 5,
     *   "message": "Successfully categorized 5 transactions"
     * }
     *
     * @param authentication Spring Security authentication (JWT)
     * @param accountId account ID (for verification)
     * @param request bulk recategorize request
     * @return result summary (200 OK)
     */
    @PostMapping("/recategorize")
    public ResponseEntity<BulkRecategorizeResponseDto> bulkRecategorize(
            Authentication authentication,
            @RequestParam Long accountId,
            @Valid @RequestBody BulkRecategorizeRequest request) {

        log.info("POST /api/transactions/recategorize - User: {}, AccountId: {}, " +
                        "TransactionCount: {}, CategoryId: {}",
                authentication.getName(), accountId, request.getTransactionIds().size(),
                request.getCategoryId());

        Long userId = extractUserIdFromAuthentication(authentication);

        int updatedCount = transactionService.bulkUpdateCategory(
                accountId, request.getTransactionIds(), request.getCategoryId());

        BulkRecategorizeResponseDto response = BulkRecategorizeResponseDto.builder()
                .updatedCount(updatedCount)
                .categoryId(request.getCategoryId())
                .message(String.format("Successfully categorized %d transactions", updatedCount))
                .build();

        log.info("Bulk categorization completed: {} transactions updated", updatedCount);

        return ResponseEntity.ok(response);
    }

    /**
     * Extract user ID from Authentication principal.
     * TODO: Extract from JWT token's subject claim in production.
     *
     * @param authentication Spring Security authentication
     * @return user ID
     */
    private Long extractUserIdFromAuthentication(Authentication authentication) {
        // Placeholder: In production, extract from JWT token
        return 1L; // TODO: Extract from JWT token
    }
}
