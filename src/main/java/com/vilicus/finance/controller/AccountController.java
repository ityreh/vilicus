package com.vilicus.finance.controller;

import com.vilicus.finance.dto.AccountDto;
import com.vilicus.finance.dto.CreateAccountRequest;
import com.vilicus.finance.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for account management endpoints.
 *
 * All endpoints require JWT authentication.
 * Paths: /api/accounts/*
 */
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController {

    private final AccountService accountService;

    /**
     * Create a new account for the authenticated user.
     *
     * POST /api/accounts
     *
     * Request body:
     * {
     *   "name": "Checking Account",
     *   "iban": "DE89370400440532013000",
     *   "type": "BANK_ACCOUNT",
     *   "currency": "EUR"
     * }
     *
     * Response (201):
     * {
     *   "id": 1,
     *   "name": "Checking Account",
     *   "iban": "DE89370400440532013000",
     *   "type": "BANK_ACCOUNT",
     *   "currency": "EUR",
     *   "balance": 0.00,
     *   "status": "active",
     *   "createdAt": "2026-08-12T19:30:00",
     *   "updatedAt": "2026-08-12T19:30:00"
     * }
     *
     * @param authentication Spring Security authentication (JWT)
     * @param request create account request
     * @return created account response (201 Created)
     * @throws IllegalArgumentException if validation fails (400)
     */
    @PostMapping
    public ResponseEntity<AccountDto> createAccount(
            Authentication authentication,
            @Valid @RequestBody CreateAccountRequest request) {

        log.info("POST /api/accounts - Creating account for user: {}", authentication.getName());

        // Extract user ID from JWT token (subject claim)
        // In production, this would be extracted from the token's subject claim
        // For now, we'll use a placeholder — this should be enhanced with proper JWT parsing
        Long userId = extractUserIdFromAuthentication(authentication);

        AccountDto createdAccount = accountService.createAccount(userId, request);

        log.info("Account created successfully: ID={}, IBAN={}", createdAccount.getId(), createdAccount.getIban());

        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    /**
     * Extract user ID from Authentication principal.
     *
     * TODO: This should be extracted from JWT token's subject claim in a real application.
     * For now, we're using a simplified version.
     *
     * @param authentication Spring Security authentication
     * @return user ID
     */
    private Long extractUserIdFromAuthentication(Authentication authentication) {
        // In a real app, this would parse the JWT token and extract the user ID
        // For Phase 2, we'll use a mock implementation that can be improved later
        // This will be handled by a JwtUtil extension in Phase 2.2

        // Placeholder: In tests, this will be mocked
        // In production, extract from token's subject or custom claim
        return 1L; // TODO: Extract from JWT token
    }
}
