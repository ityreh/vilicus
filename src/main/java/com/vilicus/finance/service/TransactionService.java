package com.vilicus.finance.service;

import com.vilicus.finance.dto.TransactionDto;
import com.vilicus.finance.entity.Transaction;
import com.vilicus.finance.exception.ResourceNotFoundException;
import com.vilicus.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * TransactionService — Business logic for transaction operations.
 *
 * Handles:
 * - Retrieving transactions by account
 * - Filtering by date range, category, counterparty
 * - Updating transaction categories
 * - Bulk categorization
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;

    /**
     * Get a single transaction by ID with ownership verification.
     *
     * @param accountId account ID (for verification)
     * @param transactionId transaction ID
     * @return transaction DTO
     * @throws ResourceNotFoundException if not found or not owned by account
     */
    @Transactional(readOnly = true)
    public TransactionDto getTransaction(Long accountId, Long transactionId) {
        log.debug("Retrieving transaction {} for account {}", transactionId, accountId);

        Transaction tx = transactionRepository.findByIdAndAccountId(transactionId, accountId)
                .orElseThrow(() -> {
                    log.warn("Transaction not found: transactionId={}, accountId={}", transactionId, accountId);
                    return new ResourceNotFoundException("Transaction not found");
                });

        return mapToDto(tx);
    }

    /**
     * List all transactions for an account (paginated).
     *
     * @param accountId account ID
     * @param pageable pagination info
     * @return page of transaction DTOs
     */
    @Transactional(readOnly = true)
    public Page<TransactionDto> listTransactions(Long accountId, Pageable pageable) {
        log.debug("Listing transactions for account {} with pagination", accountId);

        Page<Transaction> transactions = transactionRepository.findByAccountId(accountId, pageable);
        return transactions.map(this::mapToDto);
    }

    /**
     * List transactions by date range.
     *
     * @param accountId account ID
     * @param startDate from date (inclusive)
     * @param endDate to date (inclusive)
     * @return list of transaction DTOs
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> listTransactionsByDateRange(Long accountId, LocalDate startDate, LocalDate endDate) {
        log.debug("Listing transactions for account {} from {} to {}", accountId, startDate, endDate);

        List<Transaction> transactions = transactionRepository.findByAccountIdAndDateRange(accountId, startDate, endDate);
        return transactions.stream().map(this::mapToDto).toList();
    }

    /**
     * List transactions in a specific category.
     *
     * @param accountId account ID
     * @param categoryId category ID
     * @return list of transaction DTOs
     */
    @Transactional(readOnly = true)
    public List<TransactionDto> listTransactionsByCategory(Long accountId, Long categoryId) {
        log.debug("Listing transactions for account {} in category {}", accountId, categoryId);

        List<Transaction> transactions = transactionRepository.findByAccountIdAndCategoryIdOrderByTxDateDesc(accountId, categoryId);
        return transactions.stream().map(this::mapToDto).toList();
    }

    /**
     * Update transaction category.
     *
     * @param accountId account ID
     * @param transactionId transaction ID
     * @param categoryId new category ID
     * @throws ResourceNotFoundException if transaction not found
     */
    @Transactional
    public TransactionDto updateTransactionCategory(Long accountId, Long transactionId, Long categoryId) {
        log.debug("Updating transaction {} category to {}", transactionId, categoryId);

        // Verify transaction exists and belongs to account
        Transaction tx = transactionRepository.findByIdAndAccountId(transactionId, accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        tx.categorize(categoryId);
        Transaction updated = transactionRepository.save(tx);

        log.info("Transaction {} categorized as {}", transactionId, categoryId);
        return mapToDto(updated);
    }

    /**
     * Bulk update categories for multiple transactions.
     *
     * @param accountId account ID
     * @param transactionIds transaction IDs to update
     * @param categoryId category to assign
     * @return count of updated transactions
     */
    @Transactional
    public int bulkUpdateCategory(Long accountId, List<Long> transactionIds, Long categoryId) {
        log.debug("Bulk updating {} transactions to category {}", transactionIds.size(), categoryId);

        int updated = transactionRepository.updateCategoriesInBatch(transactionIds, categoryId, "categorized");
        log.info("Bulk categorized {} transactions", updated);

        return updated;
    }

    /**
     * Archive a transaction (soft delete).
     *
     * @param accountId account ID
     * @param transactionId transaction ID
     * @throws ResourceNotFoundException if not found
     */
    @Transactional
    public void archiveTransaction(Long accountId, Long transactionId) {
        log.debug("Archiving transaction {}", transactionId);

        Transaction tx = transactionRepository.findByIdAndAccountId(transactionId, accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found"));

        tx.archive();
        transactionRepository.save(tx);

        log.info("Transaction {} archived", transactionId);
    }

    /**
     * Count transactions by status.
     *
     * @param accountId account ID
     * @param status status filter ("imported", "categorized", "archived")
     * @return count
     */
    @Transactional(readOnly = true)
    public long countByStatus(Long accountId, String status) {
        return transactionRepository.countByAccountIdAndStatus(accountId, status);
    }

    /**
     * Map Transaction entity to DTO.
     */
    private TransactionDto mapToDto(Transaction tx) {
        return TransactionDto.builder()
                .id(tx.getId())
                .accountId(tx.getAccountId())
                .txId(tx.getTxId())
                .txDate(tx.getTxDate())
                .amount(tx.getAmount())
                .balance(tx.getBalance())
                .direction(tx.getDirection())
                .description(tx.getDescription())
                .counterparty(tx.getCounterparty())
                .reference(tx.getReference())
                .categoryId(tx.getCategoryId())
                .status(tx.getStatus())
                .notes(tx.getNotes())
                .importSource(tx.getImportSource())
                .createdAt(tx.getCreatedAt())
                .updatedAt(tx.getUpdatedAt())
                .build();
    }
}
