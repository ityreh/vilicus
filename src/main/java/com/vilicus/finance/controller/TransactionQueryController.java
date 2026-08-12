package com.vilicus.finance.controller;

import com.vilicus.finance.dto.TransactionDto;
import com.vilicus.finance.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for transaction query endpoints.
 *
 * All endpoints require JWT authentication.
 * Paths: /api/transactions/*
 *
 * Supports filtering, pagination, and sorting.
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionQueryController {

    private final TransactionService transactionService;

    /**
     * List all transactions for the authenticated user's account.
     *
     * GET /api/transactions
     *
     * Query Parameters:
     * - accountId (required): account to query
     * - page (optional, default 0): page number (0-indexed)
     * - size (optional, default 20): page size
     * - sort (optional, default "txDate,desc"): sort by field and direction
     *   Examples: "txDate,desc", "amount,asc"
     *   Valid fields: txDate, amount, description, counterparty, createdAt
     *
     * Response (200):
     * {
     *   "content": [
     *     {
     *       "id": 1,
     *       "txDate": "2026-08-10",
     *       "amount": 100.50,
     *       "direction": "DEBIT",
     *       "description": "Invoice Payment",
     *       "counterparty": "Supplier A",
     *       ...
     *     }
     *   ],
     *   "pageable": { "pageNumber": 0, "pageSize": 20, ... },
     *   "totalElements": 150,
     *   "totalPages": 8
     * }
     *
     * @param authentication Spring Security authentication (JWT)
     * @param accountId account ID to query
     * @param page page number (default 0)
     * @param size page size (default 20)
     * @param sortBy sort specification (default "txDate,desc")
     * @return page of transactions (200 OK)
     */
    @GetMapping
    public ResponseEntity<Page<TransactionDto>> listTransactions(
            Authentication authentication,
            @RequestParam Long accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "txDate,desc") String sortBy) {

        log.info("GET /api/transactions - User: {}, AccountId: {}, Page: {}, Size: {}",
                authentication.getName(), accountId, page, size);

        Long userId = extractUserIdFromAuthentication(authentication);

        // Parse sort specification (e.g., "txDate,desc" -> field=txDate, direction=DESC)
        String[] sortParts = sortBy.split(",");
        String sortField = sortParts.length > 0 ? sortParts[0] : "txDate";
        Sort.Direction sortDirection = sortParts.length > 1 && sortParts[1].equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));
        Page<TransactionDto> transactions = transactionService.listTransactions(accountId, pageable);

        log.info("Retrieved {} transactions (page {}/{}) for account {}",
                transactions.getNumberOfElements(), page, transactions.getTotalPages(), accountId);

        return ResponseEntity.ok(transactions);
    }

    /**
     * List transactions with date range filter.
     *
     * GET /api/transactions/filter
     *
     * Query Parameters:
     * - accountId (required): account to query
     * - dateFrom (required): start date (ISO format: YYYY-MM-DD)
     * - dateTo (required): end date (ISO format: YYYY-MM-DD)
     * - categoryId (optional): filter by category ID
     * - page (optional, default 0): page number
     * - size (optional, default 20): page size
     *
     * Response (200): Array of transactions in date range
     *
     * @param authentication Spring Security authentication
     * @param accountId account ID
     * @param dateFrom start date (inclusive)
     * @param dateTo end date (inclusive)
     * @param categoryId optional category filter
     * @param page page number
     * @param size page size
     * @return list of transactions (200 OK)
     */
    @GetMapping("/filter")
    public ResponseEntity<List<TransactionDto>> filterByDateRange(
            Authentication authentication,
            @RequestParam Long accountId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) Long categoryId) {

        log.info("GET /api/transactions/filter - User: {}, AccountId: {}, DateFrom: {}, DateTo: {}",
                authentication.getName(), accountId, dateFrom, dateTo);

        Long userId = extractUserIdFromAuthentication(authentication);

        // If category filter provided, return only that category
        if (categoryId != null) {
            log.debug("Filtering by category: {}", categoryId);
            List<TransactionDto> categoryTransactions = transactionService.listTransactionsByCategory(accountId, categoryId);
            // Filter by date range
            return ResponseEntity.ok(categoryTransactions.stream()
                    .filter(t -> !t.getTxDate().isBefore(dateFrom) && !t.getTxDate().isAfter(dateTo))
                    .toList());
        }

        List<TransactionDto> transactions = transactionService.listTransactionsByDateRange(
                accountId, dateFrom, dateTo);

        log.info("Retrieved {} transactions in date range", transactions.size());

        return ResponseEntity.ok(transactions);
    }

    /**
     * List transactions by category.
     *
     * GET /api/transactions/by-category/{categoryId}
     *
     * Query Parameters:
     * - accountId (required): account to query
     *
     * Response (200): Array of transactions in specified category
     *
     * @param authentication Spring Security authentication
     * @param accountId account ID
     * @param categoryId category ID
     * @return list of transactions (200 OK)
     */
    @GetMapping("/by-category/{categoryId}")
    public ResponseEntity<List<TransactionDto>> getByCategory(
            Authentication authentication,
            @RequestParam Long accountId,
            @PathVariable Long categoryId) {

        log.info("GET /api/transactions/by-category/{} - User: {}, AccountId: {}",
                categoryId, authentication.getName(), accountId);

        Long userId = extractUserIdFromAuthentication(authentication);

        List<TransactionDto> transactions = transactionService.listTransactionsByCategory(accountId, categoryId);

        log.info("Retrieved {} transactions for category {}", transactions.size(), categoryId);

        return ResponseEntity.ok(transactions);
    }

    /**
     * Get a single transaction by ID.
     *
     * GET /api/transactions/{id}
     *
     * Query Parameters:
     * - accountId (required): account to verify ownership
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
     *   "status": "imported",
     *   ...
     * }
     *
     * @param authentication Spring Security authentication
     * @param accountId account ID (for verification)
     * @param id transaction ID
     * @return transaction DTO (200 OK) or 404 if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDto> getTransaction(
            Authentication authentication,
            @RequestParam Long accountId,
            @PathVariable Long id) {

        log.info("GET /api/transactions/{} - User: {}, AccountId: {}",
                id, authentication.getName(), accountId);

        Long userId = extractUserIdFromAuthentication(authentication);

        TransactionDto transaction = transactionService.getTransaction(accountId, id);

        log.info("Retrieved transaction {}", id);

        return ResponseEntity.ok(transaction);
    }

    /**
     * Get transaction statistics for an account.
     *
     * GET /api/transactions/stats
     *
     * Query Parameters:
     * - accountId (required): account to query
     *
     * Response (200):
     * {
     *   "imported": 45,
     *   "categorized": 125,
     *   "archived": 5
     * }
     *
     * @param authentication Spring Security authentication
     * @param accountId account ID
     * @return transaction statistics (200 OK)
     */
    @GetMapping("/stats")
    public ResponseEntity<TransactionStatsDto> getStats(
            Authentication authentication,
            @RequestParam Long accountId) {

        log.info("GET /api/transactions/stats - User: {}, AccountId: {}",
                authentication.getName(), accountId);

        Long userId = extractUserIdFromAuthentication(authentication);

        long importedCount = transactionService.countByStatus(accountId, "imported");
        long categorizedCount = transactionService.countByStatus(accountId, "categorized");
        long archivedCount = transactionService.countByStatus(accountId, "archived");

        TransactionStatsDto stats = TransactionStatsDto.builder()
                .imported(importedCount)
                .categorized(categorizedCount)
                .archived(archivedCount)
                .total(importedCount + categorizedCount + archivedCount)
                .build();

        log.info("Transaction stats for account {}: imported={}, categorized={}, archived={}",
                accountId, importedCount, categorizedCount, archivedCount);

        return ResponseEntity.ok(stats);
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

    /**
     * DTO for transaction statistics response.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class TransactionStatsDto {
        private long imported;
        private long categorized;
        private long archived;
        private long total;
    }
}
