package com.vilicus.finance.controller;

import com.vilicus.finance.dto.ImportPreviewDto;
import com.vilicus.finance.dto.ImportResultDto;
import com.vilicus.finance.service.ImportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for transaction import endpoints.
 *
 * All endpoints require JWT authentication.
 * Paths: /api/accounts/{id}/import/*
 */
@RestController
@RequestMapping("/api/accounts/{accountId}/import")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

    private final ImportService importService;

    /**
     * Generate import preview from uploaded CAMT.052 file.
     *
     * POST /api/accounts/{accountId}/import/preview
     *
     * Request: multipart/form-data with file field
     * File formats: .xml (CAMT.052)
     * File size: max 25MB
     *
     * Response (200):
     * {
     *   "importId": "uuid-1234",
     *   "fileName": "statement.xml",
     *   "fileFormat": "CAMT.052",
     *   "iban": "DE89370400440532013000",
     *   "openingBalance": 1000.00,
     *   "closingBalance": 1500.00,
     *   "statementDateFrom": "2026-08-01",
     *   "statementDateTo": "2026-08-31",
     *   "newTransactionCount": 15,
     *   "duplicateTransactionCount": 3,
     *   "totalTransactionCount": 18,
     *   "transactionSample": [
     *     {
     *       "txDate": "2026-08-10",
     *       "amount": 100.50,
     *       "direction": "DEBIT",
     *       "description": "Payment to Supplier",
     *       "counterparty": "ACME Corp",
     *       "status": "new"
     *     }
     *   ],
     *   "warnings": [],
     *   "canProceed": true
     * }
     *
     * @param authentication Spring Security authentication (JWT)
     * @param accountId account ID to import to
     * @param file uploaded CAMT.052 XML file
     * @return import preview (200 OK)
     * @throws CamtParseException if file parsing fails (400)
     */
    @PostMapping("/preview")
    public ResponseEntity<ImportPreviewDto> generatePreview(
            Authentication authentication,
            @PathVariable Long accountId,
            @RequestParam("file") MultipartFile file) {

        log.info("POST /api/accounts/{}/import/preview - User: {}, File: {}",
                accountId, authentication.getName(), file.getOriginalFilename());

        Long userId = extractUserIdFromAuthentication(authentication);
        ImportPreviewDto preview = importService.generatePreview(userId, accountId, file);

        log.info("Preview generated: {} new transactions", preview.getNewTransactionCount());

        return ResponseEntity.ok(preview);
    }

    /**
     * Confirm import and save transactions to database.
     *
     * POST /api/accounts/{accountId}/import/confirm
     *
     * Request body:
     * {
     *   "importId": "uuid-1234"
     * }
     *
     * Response (200):
     * {
     *   "importedCount": 15,
     *   "duplicateSkipped": 3,
     *   "statementDateFrom": "2026-08-01",
     *   "statementDateTo": "2026-08-31",
     *   "message": "Successfully imported 15 transactions"
     * }
     *
     * @param authentication Spring Security authentication (JWT)
     * @param accountId account ID to import to
     * @param request confirmation request with importId
     * @return import result (200 OK)
     */
    @PostMapping("/confirm")
    public ResponseEntity<ImportResultDto> confirmImport(
            Authentication authentication,
            @PathVariable Long accountId,
            @RequestBody ImportConfirmRequest request) {

        log.info("POST /api/accounts/{}/import/confirm - User: {}, ImportId: {}",
                accountId, authentication.getName(), request.getImportId());

        Long userId = extractUserIdFromAuthentication(authentication);
        ImportResultDto result = importService.confirmImport(userId, accountId, request.getImportId());

        log.info("Import confirmed: {} transactions imported", result.getImportedCount());

        return ResponseEntity.ok(result);
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
     * Request DTO for confirm import endpoint.
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ImportConfirmRequest {
        private String importId;
    }
}
