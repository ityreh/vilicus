package com.vilicus.finance.service;

import com.vilicus.finance.dto.AccountDto;
import com.vilicus.finance.dto.CreateAccountRequest;
import com.vilicus.finance.dto.UpdateAccountRequest;
import com.vilicus.finance.entity.Account;
import com.vilicus.finance.entity.AccountType;
import com.vilicus.finance.exception.ResourceNotFoundException;
import com.vilicus.finance.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.IllegalFormatException;

/**
 * Service layer for account operations.
 *
 * Handles business logic:
 * - Validation
 * - User ownership verification
 * - Transaction management
 * - Exception handling
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;

    /**
     * Create a new account for the authenticated user.
     *
     * Validations:
     * - IBAN format validation
     * - Duplicate IBAN check (per user)
     * - Duplicate name check (per user)
     * - Valid currency code
     * - Valid account type
     *
     * @param userId the authenticated user ID
     * @param request create account request DTO
     * @return created account DTO
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public AccountDto createAccount(Long userId, CreateAccountRequest request) {
        log.debug("Creating account for user: {} with IBAN: {}", userId, request.getIban());

        // Validate IBAN format
        if (!isValidIbanFormat(request.getIban())) {
            log.warn("Invalid IBAN format: {}", request.getIban());
            throw new IllegalArgumentException("Invalid IBAN format");
        }

        // Check for duplicate IBAN (per user)
        if (accountRepository.existsByUserIdAndIban(userId, request.getIban())) {
            log.warn("Duplicate IBAN for user {}: {}", userId, request.getIban());
            throw new IllegalArgumentException("You already have an account with this IBAN");
        }

        // Check for duplicate name (per user)
        if (accountRepository.existsByUserIdAndName(userId, request.getName())) {
            log.warn("Duplicate account name for user {}: {}", userId, request.getName());
            throw new IllegalArgumentException("You already have an account with this name");
        }

        // Validate account type
        AccountType accountType;
        try {
            accountType = AccountType.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid account type: {}", request.getType());
            throw new IllegalArgumentException("Invalid account type: " + request.getType());
        }

        // Validate currency code (simple check: 3 uppercase letters)
        if (!request.getCurrency().matches("^[A-Z]{3}$")) {
            log.warn("Invalid currency code: {}", request.getCurrency());
            throw new IllegalArgumentException("Currency must be 3 uppercase letters (ISO 4217)");
        }

        // Create account entity
        Account account = Account.builder()
                .userId(userId)
                .iban(request.getIban())
                .name(request.getName())
                .type(accountType)
                .currency(request.getCurrency())
                .balance(BigDecimal.ZERO) // Default balance
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Persist to database
        Account savedAccount = accountRepository.save(account);
        log.info("Account created successfully for user {}: ID={}", userId, savedAccount.getId());

        // Convert to DTO and return
        return mapToDto(savedAccount);
    }

    /**
     * Retrieve account by ID (with ownership verification).
     *
     * @param userId authenticated user ID
     * @param accountId account ID to retrieve
     * @return account DTO
     * @throws ResourceNotFoundException if account not found or not owned by user
     */
    @Transactional(readOnly = true)
    public AccountDto getAccount(Long userId, Long accountId) {
        log.debug("Retrieving account {} for user {}", accountId, userId);

        Account account = accountRepository.findByUserIdAndId(userId, accountId)
                .orElseThrow(() -> {
                    log.warn("Account not found or user mismatch: userId={}, accountId={}", userId, accountId);
                    return new ResourceNotFoundException("Account not found");
                });

        return mapToDto(account);
    }

    /**
     * Validate IBAN format using regex (simplified).
     *
     * Format: 2 letters (country) + 2 digits (check) + up to 30 alphanumeric
     *
     * @param iban IBAN to validate
     * @return true if valid format
     */
    private boolean isValidIbanFormat(String iban) {
        if (iban == null || iban.length() < 15 || iban.length() > 34) {
            return false;
        }
        // IBAN format: 2 letters + 2 digits + alphanumeric
        return iban.matches("^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$");
    }

    /**
     * List all accounts for the authenticated user.
     *
     * @param userId authenticated user ID
     * @return list of account DTOs (ordered by creation date, newest first)
     */
    @Transactional(readOnly = true)
    public java.util.List<AccountDto> listAccounts(Long userId) {
        log.debug("Listing accounts for user: {}", userId);

        java.util.List<Account> accounts = accountRepository.findByUserIdOrderByCreatedAtDesc(userId);

        return accounts.stream()
                .map(this::mapToDto)
                .toList();
    }

    /**
     * Update an existing account (mutable fields only).
     *
     * Mutable fields: name, type, currency
     * Immutable fields: iban, userId, balance (cannot be changed)
     *
     * @param userId authenticated user ID
     * @param accountId account ID to update
     * @param request update request (name, type, currency)
     * @return updated account DTO
     * @throws ResourceNotFoundException if account not found or not owned by user
     * @throws IllegalArgumentException if validation fails
     */
    @Transactional
    public AccountDto updateAccount(Long userId, Long accountId, UpdateAccountRequest request) {
        log.debug("Updating account {} for user {}", accountId, userId);

        // Retrieve account (with ownership check)
        Account account = accountRepository.findByUserIdAndId(userId, accountId)
                .orElseThrow(() -> {
                    log.warn("Account not found or user mismatch: userId={}, accountId={}", userId, accountId);
                    return new ResourceNotFoundException("Account not found");
                });

        // Validate account type
        AccountType accountType;
        try {
            accountType = AccountType.valueOf(request.getType());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid account type: {}", request.getType());
            throw new IllegalArgumentException("Invalid account type: " + request.getType());
        }

        // Validate currency code
        if (!request.getCurrency().matches("^[A-Z]{3}$")) {
            log.warn("Invalid currency code: {}", request.getCurrency());
            throw new IllegalArgumentException("Currency must be 3 uppercase letters (ISO 4217)");
        }

        // Check for duplicate name (if name is changing)
        if (!account.getName().equals(request.getName()) &&
            accountRepository.existsByUserIdAndName(userId, request.getName())) {
            log.warn("Duplicate account name for user {}: {}", userId, request.getName());
            throw new IllegalArgumentException("You already have an account with this name");
        }

        // Update mutable fields
        account.setName(request.getName());
        account.setType(accountType);
        account.setCurrency(request.getCurrency());
        account.setUpdatedAt(LocalDateTime.now());

        // Save and return
        Account updated = accountRepository.save(account);
        log.info("Account updated successfully: ID={}", accountId);

        return mapToDto(updated);
    }

    /**
     * Archive (soft delete) an account.
     *
     * Changes status from 'active' to 'archived'.
     * Data remains in database but is marked as inactive.
     *
     * @param userId authenticated user ID
     * @param accountId account ID to archive
     * @throws ResourceNotFoundException if account not found or not owned by user
     */
    @Transactional
    public void archiveAccount(Long userId, Long accountId) {
        log.debug("Archiving account {} for user {}", accountId, userId);

        // Retrieve account (with ownership check)
        Account account = accountRepository.findByUserIdAndId(userId, accountId)
                .orElseThrow(() -> {
                    log.warn("Account not found or user mismatch: userId={}, accountId={}", userId, accountId);
                    return new ResourceNotFoundException("Account not found");
                });

        // Soft delete: mark as archived
        account.archive();

        // Save
        accountRepository.save(account);
        log.info("Account archived successfully: ID={}", accountId);
    }

    /**
     * Map Account entity to AccountDto.
     *
     * @param account Account entity
     * @return AccountDto
     */
    private AccountDto mapToDto(Account account) {
        return AccountDto.builder()
                .id(account.getId())
                .name(account.getName())
                .iban(account.getIban())
                .type(account.getType().name())
                .currency(account.getCurrency())
                .balance(account.getBalance())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }
}
