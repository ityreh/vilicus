package com.vilicus.finance.service;

import com.vilicus.finance.dto.ImportPreviewDto;
import com.vilicus.finance.dto.ImportResultDto;
import com.vilicus.finance.entity.Account;
import com.vilicus.finance.entity.Transaction;
import com.vilicus.finance.exception.CamtParseException;
import com.vilicus.finance.exception.ResourceNotFoundException;
import com.vilicus.finance.model.camt.CamtTransaction;
import com.vilicus.finance.repository.AccountRepository;
import com.vilicus.finance.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * ImportService — Orchestrates transaction import workflow.
 *
 * Workflow:
 * 1. User uploads file (CSV, OFX, or CAMT.052)
 * 2. Backend parses file → validates format → checks for duplicates
 * 3. Return ImportPreview to user (what will be imported)
 * 4. User reviews and clicks "confirm"
 * 5. Backend saves transactions to database → update account balance
 *
 * This service handles step 2-3 (preview generation).
 * Step 5 will be handled by a separate confirmImport() method.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ImportService {

    private final CamtParser camtParser;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    // In-memory cache for import previews (temporary storage)
    // In production, use Redis or database session table
    private final Map<String, ImportPreviewData> previewCache = new HashMap<>();

    /**
     * Generate import preview for uploaded CAMT.052 file.
     *
     * @param userId authenticated user ID
     * @param accountId account to import to
     * @param file uploaded XML file
     * @return ImportPreviewDto showing what will be imported
     * @throws CamtParseException if file parsing fails
     * @throws ResourceNotFoundException if account not found
     */
    @Transactional(readOnly = true)
    public ImportPreviewDto generatePreview(Long userId, Long accountId, MultipartFile file) throws CamtParseException {
        log.info("Generating import preview for account {} (file: {})", accountId, file.getOriginalFilename());

        // Verify account exists and belongs to user
        Account account = accountRepository.findByUserIdAndId(userId, accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Validate file
        if (file.isEmpty()) {
            throw new CamtParseException("Uploaded file is empty");
        }

        if (!isValidXmlFile(file)) {
            throw new CamtParseException("File must be a valid CAMT.052 XML file (.xml)");
        }

        // Parse CAMT file
        CamtParser.CamtParseResult parseResult;
        try {
            parseResult = camtParser.parseInputStream(file.getInputStream());
        } catch (IOException e) {
            throw new CamtParseException("Failed to read uploaded file: " + e.getMessage(), e);
        }

        if (parseResult.getTransactions().isEmpty()) {
            throw new CamtParseException("No transactions found in the uploaded file");
        }

        // Validate IBAN match
        List<String> warnings = new ArrayList<>();
        boolean canProceed = true;

        if (parseResult.getIban() != null && !parseResult.getIban().equals(account.getIban())) {
            warnings.add(String.format("⚠️ IBAN mismatch: File contains %s, but account is %s",
                    parseResult.getIban(), account.getIban()));
            canProceed = false; // Cannot import if IBANs don't match
        }

        // Check for duplicates and count new transactions
        int newCount = 0;
        int duplicateCount = 0;
        List<ImportPreviewDto.TransactionPreviewDto> sampleTransactions = new ArrayList<>();

        for (CamtTransaction camtTx : parseResult.getTransactions()) {
            if (camtTx.getTxId() != null && transactionRepository.existsByAccountIdAndTxId(accountId, camtTx.getTxId())) {
                duplicateCount++;
            } else {
                newCount++;

                // Add to sample (first 10)
                if (sampleTransactions.size() < 10) {
                    sampleTransactions.add(mapToPreviewDto(camtTx, false));
                }
            }
        }

        // Generate import ID (for cache)
        String importId = UUID.randomUUID().toString();

        // Cache the preview data (for later confirm request)
        ImportPreviewData cacheData = ImportPreviewData.builder()
                .userId(userId)
                .accountId(accountId)
                .file(file)
                .parseResult(parseResult)
                .build();
        previewCache.put(importId, cacheData);

        // Build response DTO
        ImportPreviewDto preview = ImportPreviewDto.builder()
                .importId(importId)
                .fileName(file.getOriginalFilename())
                .fileFormat("CAMT.052")
                .iban(parseResult.getIban())
                .openingBalance(parseResult.getOpeningBalance())
                .closingBalance(parseResult.getClosingBalance())
                .statementDateFrom(parseResult.getStatementDateFrom())
                .statementDateTo(parseResult.getStatementDateTo())
                .newTransactionCount(newCount)
                .duplicateTransactionCount(duplicateCount)
                .totalTransactionCount(parseResult.getTransactions().size())
                .transactionSample(sampleTransactions)
                .warnings(warnings)
                .canProceed(canProceed)
                .build();

        log.info("Preview generated: {} new transactions, {} duplicates",
                newCount, duplicateCount);

        return preview;
    }

    /**
     * Confirm and save the import preview.
     *
     * @param userId authenticated user ID
     * @param accountId account to import to
     * @param importId import session ID from preview
     * @return ImportResultDto with count of imported transactions
     */
    @Transactional
    public ImportResultDto confirmImport(Long userId, Long accountId, String importId) {
        log.info("Confirming import {} for account {}", importId, accountId);

        // Retrieve cached preview data
        ImportPreviewData previewData = previewCache.get(importId);
        if (previewData == null) {
            throw new IllegalArgumentException("Invalid or expired import session");
        }

        // Verify import belongs to requesting user
        if (!previewData.getUserId().equals(userId) || !previewData.getAccountId().equals(accountId)) {
            throw new IllegalArgumentException("Import session does not match current user/account");
        }

        // Retrieve account
        Account account = accountRepository.findByUserIdAndId(userId, accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        // Save transactions to database (skip duplicates)
        CamtParser.CamtParseResult parseResult = previewData.getParseResult();
        List<Transaction> importedTransactions = new ArrayList<>();
        int duplicateSkipped = 0;

        for (CamtTransaction camtTx : parseResult.getTransactions()) {
            // Skip if already exists (by txId)
            if (camtTx.getTxId() != null && transactionRepository.existsByAccountIdAndTxId(accountId, camtTx.getTxId())) {
                duplicateSkipped++;
                continue;
            }

            // Convert to Transaction entity
            Transaction tx = Transaction.builder()
                    .accountId(accountId)
                    .txId(camtTx.getTxId())
                    .txDate(camtTx.getTxDate())
                    .amount(camtTx.getAmount())
                    .balance(camtTx.getBalance())
                    .direction(camtTx.getDirection())
                    .description(camtTx.getDescription())
                    .counterparty(camtTx.getCounterparty())
                    .reference(camtTx.getReference())
                    .importSource("CAMT.052")
                    .status("imported")
                    .build();

            // Validate transaction
            if (!tx.isValid()) {
                log.warn("Skipping invalid transaction: {}", camtTx);
                continue;
            }

            importedTransactions.add(tx);
        }

        // Batch save
        transactionRepository.saveAll(importedTransactions);
        log.info("Imported {} transactions (skipped {} duplicates)", importedTransactions.size(), duplicateSkipped);

        // Update account balance if available
        if (parseResult.getClosingBalance() != null) {
            account.setBalance(parseResult.getClosingBalance());
            accountRepository.save(account);
            log.info("Updated account balance to: {}", parseResult.getClosingBalance());
        }

        // Clean up cache
        previewCache.remove(importId);

        return com.vilicus.finance.dto.ImportResultDto.builder()
                .importedCount(importedTransactions.size())
                .duplicateSkipped(duplicateSkipped)
                .statementDateFrom(parseResult.getStatementDateFrom())
                .statementDateTo(parseResult.getStatementDateTo())
                .message(String.format("Successfully imported %d transactions", importedTransactions.size()))
                .build();
    }

    /**
     * Map CamtTransaction to preview DTO.
     */
    private ImportPreviewDto.TransactionPreviewDto mapToPreviewDto(CamtTransaction camtTx, boolean isDuplicate) {
        return ImportPreviewDto.TransactionPreviewDto.builder()
                .txDate(camtTx.getTxDate())
                .amount(camtTx.getAmount())
                .direction(camtTx.getDirection())
                .description(camtTx.getDescription())
                .counterparty(camtTx.getCounterparty())
                .status(isDuplicate ? "duplicate" : "new")
                .build();
    }

    /**
     * Validate that uploaded file is XML.
     */
    private boolean isValidXmlFile(MultipartFile file) {
        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();

        // Check file extension
        if (filename != null && !filename.toLowerCase().endsWith(".xml")) {
            return false;
        }

        // Check content type (can be text/xml or application/xml)
        return contentType != null && (contentType.contains("xml") || contentType.contains("text"));
    }

    /**
     * ImportPreviewData — Cached data from preview (before confirm).
     */
    @lombok.Data
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    @lombok.Builder
    public static class ImportPreviewData {
        private Long userId;
        private Long accountId;
        private MultipartFile file;
        private CamtParser.CamtParseResult parseResult;
    }
}
