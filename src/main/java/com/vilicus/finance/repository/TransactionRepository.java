package com.vilicus.finance.repository;

import com.vilicus.finance.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Transaction entity.
 *
 * All queries are scoped to user's account (account_id).
 * Ensures data isolation and security.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /**
     * Find all transactions for a specific account, ordered by date (newest first).
     * @param accountId account ID
     * @return list of transactions sorted by date descending
     */
    List<Transaction> findByAccountIdOrderByTxDateDesc(Long accountId);

    /**
     * Find all transactions for a specific account (paginated).
     * @param accountId account ID
     * @param pageable pagination info
     * @return page of transactions
     */
    Page<Transaction> findByAccountId(Long accountId, Pageable pageable);

    /**
     * Find transactions by account ID and status.
     * Used to filter: only "imported" or "categorized" transactions.
     *
     * @param accountId account ID
     * @param status transaction status ("imported", "categorized", "archived")
     * @return list of matching transactions
     */
    List<Transaction> findByAccountIdAndStatusOrderByTxDateDesc(Long accountId, String status);

    /**
     * Find transactions by account ID and date range.
     * Used for: monthly view, reporting, analytics.
     *
     * @param accountId account ID
     * @param startDate filter from date (inclusive)
     * @param endDate filter to date (inclusive)
     * @return list of transactions within date range
     */
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.txDate BETWEEN :startDate AND :endDate ORDER BY t.txDate DESC")
    List<Transaction> findByAccountIdAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * Find transactions by account ID and date range (paginated).
     * @param accountId account ID
     * @param startDate filter from date
     * @param endDate filter to date
     * @param pageable pagination info
     * @return page of transactions
     */
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.txDate BETWEEN :startDate AND :endDate ORDER BY t.txDate DESC")
    Page<Transaction> findByAccountIdAndDateRange(
            @Param("accountId") Long accountId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );

    /**
     * Find a single transaction by ID with ownership check.
     * Always verify the transaction belongs to the requesting user's account.
     *
     * @param accountId account ID
     * @param transactionId transaction ID
     * @return Optional containing transaction if found and owned by account
     */
    @Query("SELECT t FROM Transaction t WHERE t.id = :transactionId AND t.accountId = :accountId")
    Optional<Transaction> findByIdAndAccountId(
            @Param("transactionId") Long transactionId,
            @Param("accountId") Long accountId
    );

    /**
     * Find transaction by account ID and bank transaction ID.
     * Used for deduplication: check if this tx_id already exists.
     *
     * @param accountId account ID
     * @param txId bank transaction ID
     * @return Optional containing transaction if found
     */
    Optional<Transaction> findByAccountIdAndTxId(Long accountId, String txId);

    /**
     * Check if a transaction already exists (for deduplication).
     * Used during import to skip already-imported transactions.
     *
     * @param accountId account ID
     * @param txId bank transaction ID
     * @return true if transaction with this tx_id exists for account
     */
    boolean existsByAccountIdAndTxId(Long accountId, String txId);

    /**
     * Find potential duplicates by fuzzy matching.
     * Transactions with same account, date, and amount (but different tx_id).
     * Used to warn user of possible duplicates.
     *
     * @param accountId account ID
     * @param txDate transaction date
     * @param amount transaction amount
     * @return list of transactions with same date+amount
     */
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND t.txDate = :txDate AND t.amount = :amount")
    List<Transaction> findPotentialDuplicates(
            @Param("accountId") Long accountId,
            @Param("txDate") LocalDate txDate,
            @Param("amount") BigDecimal amount
    );

    /**
     * Count transactions for an account by status.
     * Used for dashboard: how many imported vs categorized?
     *
     * @param accountId account ID
     * @param status transaction status
     * @return count of transactions with this status
     */
    long countByAccountIdAndStatus(Long accountId, String status);

    /**
     * Find transactions by account ID and category ID.
     * Used for: viewing all transactions in a category, category analytics.
     *
     * @param accountId account ID
     * @param categoryId category ID
     * @return list of transactions in this category
     */
    List<Transaction> findByAccountIdAndCategoryIdOrderByTxDateDesc(Long accountId, Long categoryId);

    /**
     * Count transactions by account ID and category ID.
     * Used for: category spending summary.
     *
     * @param accountId account ID
     * @param categoryId category ID
     * @return count of transactions in this category
     */
    long countByAccountIdAndCategoryId(Long accountId, Long categoryId);

    /**
     * Find transactions by counterparty name.
     * Used for: categorization rules, vendor history.
     *
     * @param accountId account ID
     * @param counterparty counterparty name (exact match)
     * @return list of transactions from this counterparty
     */
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND UPPER(t.counterparty) = UPPER(:counterparty) ORDER BY t.txDate DESC")
    List<Transaction> findByAccountIdAndCounterparty(
            @Param("accountId") Long accountId,
            @Param("counterparty") String counterparty
    );

    /**
     * Find transactions by counterparty name (partial match, like search).
     * Used for: search functionality, autocomplete.
     *
     * @param accountId account ID
     * @param counterpartyPattern pattern to match (e.g., "Rewe%")
     * @return list of transactions matching pattern
     */
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId AND UPPER(t.counterparty) LIKE UPPER(:pattern) ORDER BY t.txDate DESC")
    List<Transaction> findByCounterpartyLike(
            @Param("accountId") Long accountId,
            @Param("pattern") String counterpartyPattern
    );

    /**
     * Update category for a single transaction.
     * Used when user manually categorizes a transaction.
     *
     * @param transactionId transaction ID
     * @param categoryId category ID to assign
     * @param status new status (usually "categorized")
     */
    @Modifying
    @Query("UPDATE Transaction t SET t.categoryId = :categoryId, t.status = :status WHERE t.id = :transactionId")
    void updateCategory(
            @Param("transactionId") Long transactionId,
            @Param("categoryId") Long categoryId,
            @Param("status") String status
    );

    /**
     * Batch update categories for multiple transactions.
     * Used for bulk recategorization.
     *
     * @param transactionIds list of transaction IDs to update
     * @param categoryId category ID to assign to all
     * @param status new status
     * @return count of updated rows
     */
    @Modifying
    @Query("UPDATE Transaction t SET t.categoryId = :categoryId, t.status = :status WHERE t.id IN :transactionIds")
    int updateCategoriesInBatch(
            @Param("transactionIds") List<Long> transactionIds,
            @Param("categoryId") Long categoryId,
            @Param("status") String status
    );

    /**
     * Get total balance (sum of all amounts) for an account.
     * Used for: dashboard summary, analytics.
     * Note: This is sum of transaction amounts, not account balance.
     *
     * @param accountId account ID
     * @return sum of all amounts
     */
    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE account_id = :accountId", nativeQuery = true)
    BigDecimal getTotalAmount(@Param("accountId") Long accountId);

    /**
     * Get sum of amounts by direction (DEBIT or CREDIT).
     * Used for: spending analytics.
     *
     * @param accountId account ID
     * @param direction "DEBIT" or "CREDIT"
     * @return sum of amounts for this direction
     */
    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM transactions WHERE account_id = :accountId AND direction = :direction", nativeQuery = true)
    BigDecimal getTotalAmountByDirection(
            @Param("accountId") Long accountId,
            @Param("direction") String direction
    );

    /**
     * Delete all transactions for an account (cascade delete).
     * Used when account is deleted.
     *
     * @param accountId account ID
     */
    @Modifying
    void deleteByAccountId(Long accountId);

    /**
     * Count total transactions for an account.
     * @param accountId account ID
     * @return number of transactions
     */
    long countByAccountId(Long accountId);

    /**
     * Find the latest transaction for an account.
     * Used to determine last import date.
     *
     * @param accountId account ID
     * @return Optional containing the newest transaction
     */
    @Query("SELECT t FROM Transaction t WHERE t.accountId = :accountId ORDER BY t.txDate DESC LIMIT 1")
    Optional<Transaction> findLatestTransaction(@Param("accountId") Long accountId);
}
