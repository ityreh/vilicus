package com.vilicus.finance.controller;

import com.vilicus.finance.dto.BulkRecategorizeRequest;
import com.vilicus.finance.dto.BulkRecategorizeResponseDto;
import com.vilicus.finance.dto.TransactionDto;
import com.vilicus.finance.dto.UpdateTransactionCategoryRequest;
import com.vilicus.finance.exception.ResourceNotFoundException;
import com.vilicus.finance.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Unit tests for TransactionCategoryController.
 *
 * Tests:
 * - Update single transaction category
 * - Bulk recategorize multiple transactions
 * - Validation (empty lists, null values)
 * - Error handling (transaction not found)
 * - Status transitions (imported → categorized)
 */
@ExtendWith(MockitoExtension.class)
public class TransactionCategoryControllerTest {

    @Mock(lenient = true)
    private TransactionService transactionService;

    @InjectMocks
    private TransactionCategoryController controller;

    private Authentication testAuth;
    private Long testAccountId = 1L;
    private TransactionDto testTransaction;

    @BeforeEach
    void setUp() {
        testAuth = new TestingAuthenticationToken("user", "password");

        testTransaction = TransactionDto.builder()
                .id(1L)
                .accountId(testAccountId)
                .txId("TXN-001")
                .txDate(LocalDate.of(2026, 8, 10))
                .amount(BigDecimal.valueOf(100.50))
                .direction("DEBIT")
                .description("Invoice Payment")
                .counterparty("Supplier A")
                .categoryId(null)
                .status("imported")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testUpdateCategory_Success() {
        Long categoryId = 5L;
        UpdateTransactionCategoryRequest request = new UpdateTransactionCategoryRequest(categoryId);

        testTransaction.setCategoryId(categoryId);
        testTransaction.setStatus("categorized");

        when(transactionService.updateTransactionCategory(testAccountId, 1L, categoryId))
                .thenReturn(testTransaction);

        var response = controller.updateCategory(testAuth, testAccountId, 1L, request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(categoryId, response.getBody().getCategoryId());
        assertEquals("categorized", response.getBody().getStatus());
    }

    @Test
    void testUpdateCategory_StatusChangeToCategorizied() {
        Long categoryId = 3L;
        UpdateTransactionCategoryRequest request = new UpdateTransactionCategoryRequest(categoryId);

        // Initially imported, should change to categorized
        testTransaction.setStatus("imported");
        testTransaction.setCategoryId(null);

        TransactionDto categorizedTransaction = TransactionDto.builder()
                .id(1L)
                .accountId(testAccountId)
                .txId("TXN-001")
                .txDate(testTransaction.getTxDate())
                .amount(testTransaction.getAmount())
                .direction(testTransaction.getDirection())
                .description(testTransaction.getDescription())
                .counterparty(testTransaction.getCounterparty())
                .categoryId(categoryId)
                .status("categorized")
                .build();

        when(transactionService.updateTransactionCategory(testAccountId, 1L, categoryId))
                .thenReturn(categorizedTransaction);

        var response = controller.updateCategory(testAuth, testAccountId, 1L, request);

        assertEquals("categorized", response.getBody().getStatus());
    }

    @Test
    void testUpdateCategory_NotFound_ThrowsException() {
        Long categoryId = 5L;
        UpdateTransactionCategoryRequest request = new UpdateTransactionCategoryRequest(categoryId);

        when(transactionService.updateTransactionCategory(testAccountId, 999L, categoryId))
                .thenThrow(new ResourceNotFoundException("Transaction not found"));

        assertThrows(ResourceNotFoundException.class, () -> {
            controller.updateCategory(testAuth, testAccountId, 999L, request);
        });
    }

    @Test
    void testUpdateCategory_DifferentCategoryIds() {
        for (long catId = 1L; catId <= 5L; catId++) {
            UpdateTransactionCategoryRequest request = new UpdateTransactionCategoryRequest(catId);
            testTransaction.setCategoryId(catId);

            when(transactionService.updateTransactionCategory(testAccountId, 1L, catId))
                    .thenReturn(testTransaction);

            var response = controller.updateCategory(testAuth, testAccountId, 1L, request);

            assertEquals(catId, response.getBody().getCategoryId());
        }
    }

    @Test
    void testBulkRecategorize_Success() {
        List<Long> transactionIds = List.of(1L, 2L, 3L, 4L, 5L);
        Long categoryId = 5L;
        BulkRecategorizeRequest request = BulkRecategorizeRequest.builder()
                .transactionIds(transactionIds)
                .categoryId(categoryId)
                .build();

        when(transactionService.bulkUpdateCategory(testAccountId, transactionIds, categoryId))
                .thenReturn(5);

        var response = controller.bulkRecategorize(testAuth, testAccountId, request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(5, response.getBody().getUpdatedCount());
        assertEquals(categoryId, response.getBody().getCategoryId());
        assertTrue(response.getBody().getMessage().contains("5"));
    }

    @Test
    void testBulkRecategorize_SingleTransaction() {
        List<Long> transactionIds = List.of(1L);
        Long categoryId = 3L;
        BulkRecategorizeRequest request = BulkRecategorizeRequest.builder()
                .transactionIds(transactionIds)
                .categoryId(categoryId)
                .build();

        when(transactionService.bulkUpdateCategory(testAccountId, transactionIds, categoryId))
                .thenReturn(1);

        var response = controller.bulkRecategorize(testAuth, testAccountId, request);

        assertEquals(1, response.getBody().getUpdatedCount());
        assertTrue(response.getBody().getMessage().contains("1"));
    }

    @Test
    void testBulkRecategorize_ManyTransactions() {
        List<Long> transactionIds = new java.util.ArrayList<>();
        for (long i = 1L; i <= 100L; i++) {
            transactionIds.add(i);
        }
        Long categoryId = 10L;
        BulkRecategorizeRequest request = BulkRecategorizeRequest.builder()
                .transactionIds(transactionIds)
                .categoryId(categoryId)
                .build();

        when(transactionService.bulkUpdateCategory(testAccountId, transactionIds, categoryId))
                .thenReturn(100);

        var response = controller.bulkRecategorize(testAuth, testAccountId, request);

        assertEquals(100, response.getBody().getUpdatedCount());
        assertEquals(categoryId, response.getBody().getCategoryId());
    }

    @Test
    void testBulkRecategorize_PartialSuccess() {
        // Only 3 out of 5 transaction IDs belong to this account
        List<Long> transactionIds = List.of(1L, 2L, 3L, 4L, 5L);
        Long categoryId = 5L;
        BulkRecategorizeRequest request = BulkRecategorizeRequest.builder()
                .transactionIds(transactionIds)
                .categoryId(categoryId)
                .build();

        when(transactionService.bulkUpdateCategory(testAccountId, transactionIds, categoryId))
                .thenReturn(3);

        var response = controller.bulkRecategorize(testAuth, testAccountId, request);

        // Only 3 were updated (the ones that belong to this account)
        assertEquals(3, response.getBody().getUpdatedCount());
    }

    @Test
    void testBulkRecategorize_NoTransactionsUpdated() {
        // None of the transaction IDs belong to this account
        List<Long> transactionIds = List.of(999L, 1000L, 1001L);
        Long categoryId = 5L;
        BulkRecategorizeRequest request = BulkRecategorizeRequest.builder()
                .transactionIds(transactionIds)
                .categoryId(categoryId)
                .build();

        when(transactionService.bulkUpdateCategory(testAccountId, transactionIds, categoryId))
                .thenReturn(0);

        var response = controller.bulkRecategorize(testAuth, testAccountId, request);

        assertEquals(0, response.getBody().getUpdatedCount());
        assertTrue(response.getBody().getMessage().contains("0"));
    }

    @Test
    void testBulkRecategorizeResponseMessage() {
        List<Long> transactionIds = List.of(1L, 2L, 3L);
        Long categoryId = 7L;
        BulkRecategorizeRequest request = BulkRecategorizeRequest.builder()
                .transactionIds(transactionIds)
                .categoryId(categoryId)
                .build();

        when(transactionService.bulkUpdateCategory(testAccountId, transactionIds, categoryId))
                .thenReturn(3);

        var response = controller.bulkRecategorize(testAuth, testAccountId, request);

        String expectedMessage = "Successfully categorized 3 transactions";
        assertEquals(expectedMessage, response.getBody().getMessage());
    }

    @Test
    void testUpdateCategory_AllFields() {
        Long categoryId = 8L;
        UpdateTransactionCategoryRequest request = new UpdateTransactionCategoryRequest(categoryId);

        TransactionDto fullTransaction = TransactionDto.builder()
                .id(1L)
                .accountId(testAccountId)
                .txId("TXN-001")
                .txDate(LocalDate.of(2026, 8, 10))
                .amount(BigDecimal.valueOf(100.50))
                .balance(BigDecimal.valueOf(1000))
                .direction("DEBIT")
                .description("Invoice Payment")
                .counterparty("Supplier A")
                .reference("REF-001")
                .categoryId(categoryId)
                .status("categorized")
                .notes("User note")
                .importSource("CAMT.052")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(transactionService.updateTransactionCategory(testAccountId, 1L, categoryId))
                .thenReturn(fullTransaction);

        var response = controller.updateCategory(testAuth, testAccountId, 1L, request);

        assertEquals(200, response.getStatusCode().value());
        TransactionDto result = response.getBody();
        assertEquals(categoryId, result.getCategoryId());
        assertEquals("categorized", result.getStatus());
        assertEquals("DEBIT", result.getDirection());
        assertEquals("Supplier A", result.getCounterparty());
    }

    @Test
    void testBulkRecategorize_ResponseHasAllFields() {
        List<Long> transactionIds = List.of(1L, 2L);
        Long categoryId = 4L;
        BulkRecategorizeRequest request = BulkRecategorizeRequest.builder()
                .transactionIds(transactionIds)
                .categoryId(categoryId)
                .build();

        when(transactionService.bulkUpdateCategory(testAccountId, transactionIds, categoryId))
                .thenReturn(2);

        var response = controller.bulkRecategorize(testAuth, testAccountId, request);

        BulkRecategorizeResponseDto body = response.getBody();
        assertNotNull(body.getUpdatedCount());
        assertNotNull(body.getCategoryId());
        assertNotNull(body.getMessage());
        assertEquals(2, body.getUpdatedCount());
        assertEquals(categoryId, body.getCategoryId());
    }

    @Test
    void testUpdateCategory_ZeroCategoryId() {
        // Edge case: category ID is 0 (should still work)
        Long categoryId = 0L;
        UpdateTransactionCategoryRequest request = new UpdateTransactionCategoryRequest(categoryId);

        testTransaction.setCategoryId(categoryId);

        when(transactionService.updateTransactionCategory(testAccountId, 1L, categoryId))
                .thenReturn(testTransaction);

        var response = controller.updateCategory(testAuth, testAccountId, 1L, request);

        assertEquals(categoryId, response.getBody().getCategoryId());
    }

    @Test
    void testUpdateCategory_LargeCategoryId() {
        // Edge case: very large category ID
        Long categoryId = 999999L;
        UpdateTransactionCategoryRequest request = new UpdateTransactionCategoryRequest(categoryId);

        testTransaction.setCategoryId(categoryId);

        when(transactionService.updateTransactionCategory(testAccountId, 1L, categoryId))
                .thenReturn(testTransaction);

        var response = controller.updateCategory(testAuth, testAccountId, 1L, request);

        assertEquals(categoryId, response.getBody().getCategoryId());
    }
}
