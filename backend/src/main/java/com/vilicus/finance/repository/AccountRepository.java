package com.vilicus.finance.repository;

import com.vilicus.finance.entity.Account;
import com.vilicus.finance.entity.AccountType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Account entity.
 *
 * Provides CRUD operations and custom queries for account management.
 * All queries are user-scoped for security (user isolation).
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Find all accounts for a specific user, ordered by creation date (newest first)
     *
     * @param userId the user ID
     * @return list of accounts owned by the user
     */
    List<Account> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Find all active accounts for a user
     *
     * @param userId the user ID
     * @return list of active accounts
     */
    List<Account> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status);

    /**
     * Find accounts by type (paginated)
     *
     * @param userId the user ID
     * @param type the account type
     * @param pageable pagination info
     * @return paginated list of accounts
     */
    Page<Account> findByUserIdAndTypeOrderByCreatedAtDesc(Long userId, AccountType type, Pageable pageable);

    /**
     * Find a specific account by user ID and account ID
     *
     * Used for security checks to ensure user owns the account
     *
     * @param userId the user ID
     * @param accountId the account ID
     * @return Optional containing the account if found and owned by user
     */
    Optional<Account> findByUserIdAndId(Long userId, Long accountId);

    /**
     * Check if a user already has an account with a specific IBAN
     *
     * Used for duplicate IBAN validation
     *
     * @param userId the user ID
     * @param iban the IBAN to check
     * @return true if account with this IBAN exists for user
     */
    boolean existsByUserIdAndIban(Long userId, String iban);

    /**
     * Check if a user already has an account with a specific name
     *
     * @param userId the user ID
     * @param name the account name to check
     * @return true if account with this name exists for user
     */
    boolean existsByUserIdAndName(Long userId, String name);

    /**
     * Find account by IBAN and user ID
     *
     * Used to retrieve account details via IBAN
     *
     * @param userId the user ID
     * @param iban the IBAN
     * @return Optional containing the account if found
     */
    Optional<Account> findByUserIdAndIban(Long userId, String iban);

    /**
     * Count active accounts for a user
     *
     * @param userId the user ID
     * @return number of active accounts
     */
    long countByUserIdAndStatus(Long userId, String status);

    /**
     * Count all accounts by type for a user
     *
     * @param userId the user ID
     * @param type the account type
     * @return count of accounts of this type
     */
    long countByUserIdAndType(Long userId, AccountType type);

    /**
     * Delete all accounts for a user (cascade on user delete)
     *
     * @param userId the user ID
     */
    void deleteByUserId(Long userId);

    /**
     * Find accounts with specific statuses (custom query)
     *
     * @param userId the user ID
     * @param statuses list of statuses to filter
     * @return list of matching accounts
     */
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.status IN :statuses ORDER BY a.createdAt DESC")
    List<Account> findAccountsByStatusList(@Param("userId") Long userId, @Param("statuses") List<String> statuses);

    /**
     * Find accounts by currency for a user
     *
     * Useful for multi-currency balance views
     *
     * @param userId the user ID
     * @param currency ISO 4217 currency code
     * @return list of accounts in specified currency
     */
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.currency = :currency ORDER BY a.createdAt DESC")
    List<Account> findByUserIdAndCurrency(@Param("userId") Long userId, @Param("currency") String currency);

    /**
     * Check total balance across all active accounts for a user
     *
     * @param userId the user ID
     * @return sum of balances or null if no active accounts
     */
    @Query(nativeQuery = true, value = """
            SELECT SUM(a.balance)
            FROM accounts a
            WHERE a.user_id = :userId AND a.status = 'active'
            """)
    Double getTotalBalance(@Param("userId") Long userId);

    /**
     * Get all accounts for a user with pagination
     *
     * @param userId the user ID
     * @param pageable pagination info
     * @return paginated accounts
     */
    Page<Account> findByUserId(Long userId, Pageable pageable);
}
