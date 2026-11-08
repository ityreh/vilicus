package com.vilicus.finance.service;

import com.vilicus.finance.dto.TransactionDto;
import com.vilicus.finance.entity.Transaction;
import com.vilicus.finance.exception.ResourceNotFoundException;
import com.vilicus.finance.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionService.
 *
 * Tests:
 * - Retrieving transactions
 * - Pagination and filtering
 * - Category updates (single and bulk)
 * - Archiving transactions
 * - Status counting
 */
@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;

    private Long testAccountId = 1L;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testTransaction = Transaction.builder()
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
                .status("imported")
                .importSource("CAMT.052")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testGetTransaction_Success() {
        when(transactionRepository.findByIdAndAccountId(1L, testAccountId))
                .thenReturn(Optional.of(testTransaction));

        TransactionDto result = transactionService.getTransaction(testAccountId, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("TXN-001", result.getTxId());
        assertEquals("DEBIT", result.getDirection());
        assertEquals("Invoice Payment", result.getDescription());
    }

    @Test
    void testGetTransaction_NotFound_ThrowsException() {
        when(transactionRepository.findByIdAndAccountId(1L, testAccountId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.getTransaction(testAccountId, 1L)
        );

        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    void testListTransactions_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Transaction tx1 = Transaction.builder()
                .id(1L)
                .accountId(testAccountId)
                .txId("TXN-001")
                .txDate(LocalDate.of(2026, 8, 10))
                .amount(BigDecimal.valueOf(100))
                .direction("DEBIT")
                .description("Payment 1")
                .status("imported")
                .build();

        Transaction tx2 = Transaction.builder()
                .id(2L)
                .accountId(testAccountId)
                .txId("TXN-002")
                .txDate(LocalDate.of(2026, 8, 15))
                .amount(BigDecimal.valueOf(50))
                .direction("CREDIT")
                .description("Payment 2")
                .status("categorized")
                .build();

        Page<Transaction> page = new PageImpl<>(List.of(tx1, tx2));
        when(transactionRepository.findByAccountId(testAccountId, pageable))
                .thenReturn(page);

        Page<TransactionDto> result = transactionService.listTransactions(testAccountId, pageable);

        assertEquals(2, result.getContent().size());
        assertEquals("TXN-001", result.getContent().get(0).getTxId());
        assertEquals("TXN-002", result.getContent().get(1).getTxId());
    }

    @Test
    void testListTransactions_Empty() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transaction> emptyPage = new PageImpl<>(List.of());

        when(transactionRepository.findByAccountId(testAccountId, pageable))
                .thenReturn(emptyPage);

        Page<TransactionDto> result = transactionService.listTransactions(testAccountId, pageable);

        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void testListTransactionsByDateRange_Success() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);

        List<Transaction> transactions = List.of(testTransaction);
        when(transactionRepository.findByAccountIdAndDateRange(testAccountId, startDate, endDate))
                .thenReturn(transactions);

        List<TransactionDto> result = transactionService.listTransactionsByDateRange(
                testAccountId, startDate, endDate);

        assertEquals(1, result.size());
        assertEquals("TXN-001", result.get(0).getTxId());
    }

    @Test
    void testListTransactionsByDateRange_Empty() {
        LocalDate startDate = LocalDate.of(2026, 8, 1);
        LocalDate endDate = LocalDate.of(2026, 8, 31);

        when(transactionRepository.findByAccountIdAndDateRange(testAccountId, startDate, endDate))
                .thenReturn(List.of());

        List<TransactionDto> result = transactionService.listTransactionsByDateRange(
                testAccountId, startDate, endDate);

        assertTrue(result.isEmpty());
    }

    @Test
    void testListTransactionsByCategory_Success() {
        Long categoryId = 5L;
        testTransaction.setCategoryId(categoryId);

        List<Transaction> transactions = List.of(testTransaction);
        when(transactionRepository.findByAccountIdAndCategoryIdOrderByTxDateDesc(testAccountId, categoryId))
                .thenReturn(transactions);

        List<TransactionDto> result = transactionService.listTransactionsByCategory(testAccountId, categoryId);

        assertEquals(1, result.size());
        assertEquals(categoryId, result.get(0).getCategoryId());
    }

    @Test
    void testUpdateTransactionCategory_Success() {
        Long newCategoryId = 3L;

        when(transactionRepository.findByIdAndAccountId(1L, testAccountId))
                .thenReturn(Optional.of(testTransaction));

        testTransaction.categorize(newCategoryId);
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(testTransaction);

        TransactionDto result = transactionService.updateTransactionCategory(
                testAccountId, 1L, newCategoryId);

        assertNotNull(result);
        assertEquals(newCategoryId, result.getCategoryId());
        assertEquals("categorized", result.getStatus());
    }

    @Test
    void testUpdateTransactionCategory_NotFound_ThrowsException() {
        when(transactionRepository.findByIdAndAccountId(1L, testAccountId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.updateTransactionCategory(testAccountId, 1L, 3L)
        );

        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    void testBulkUpdateCategory_Success() {
        List<Long> txIds = List.of(1L, 2L, 3L);
        Long categoryId = 5L;

        when(transactionRepository.updateCategoriesInBatch(txIds, categoryId, "categorized"))
                .thenReturn(3);

        int result = transactionService.bulkUpdateCategory(testAccountId, txIds, categoryId);

        assertEquals(3, result);
        verify(transactionRepository).updateCategoriesInBatch(txIds, categoryId, "categorized");
    }

    @Test
    void testBulkUpdateCategory_PartialSuccess() {
        List<Long> txIds = List.of(1L, 2L, 3L, 4L, 5L);
        Long categoryId = 5L;

        // Only 3 out of 5 were updated (2 might not belong to this account)
        when(transactionRepository.updateCategoriesInBatch(txIds, categoryId, "categorized"))
                .thenReturn(3);

        int result = transactionService.bulkUpdateCategory(testAccountId, txIds, categoryId);

        assertEquals(3, result);
    }

    @Test
    void testArchiveTransaction_Success() {
        when(transactionRepository.findByIdAndAccountId(1L, testAccountId))
                .thenReturn(Optional.of(testTransaction));

        testTransaction.archive();
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(testTransaction);

        transactionService.archiveTransaction(testAccountId, 1L);

        assertEquals("archived", testTransaction.getStatus());
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testArchiveTransaction_NotFound_ThrowsException() {
        when(transactionRepository.findByIdAndAccountId(1L, testAccountId))
                .thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.archiveTransaction(testAccountId, 1L)
        );

        assertEquals("Transaction not found", exception.getMessage());
    }

    @Test
    void testCountByStatus_Imported() {
        when(transactionRepository.countByAccountIdAndStatus(testAccountId, "imported"))
                .thenReturn(25L);

        long result = transactionService.countByStatus(testAccountId, "imported");

        assertEquals(25L, result);
    }

    @Test
    void testCountByStatus_Categorized() {
        when(transactionRepository.countByAccountIdAndStatus(testAccountId, "categorized"))
                .thenReturn(100L);

        long result = transactionService.countByStatus(testAccountId, "categorized");

        assertEquals(100L, result);
    }

    @Test
    void testCountByStatus_Archived() {
        when(transactionRepository.countByAccountIdAndStatus(testAccountId, "archived"))
                .thenReturn(5L);

        long result = transactionService.countByStatus(testAccountId, "archived");

        assertEquals(5L, result);
    }

    @Test
    void testCountByStatus_Empty() {
        when(transactionRepository.countByAccountIdAndStatus(testAccountId, "imported"))
                .thenReturn(0L);

        long result = transactionService.countByStatus(testAccountId, "imported");

        assertEquals(0L, result);
    }

    @Test
    void testTransactionDto_AllFields() {
        testTransaction.setCategoryId(10L);
        testTransaction.setNotes("User note");
        testTransaction.setStatus("categorized");

        when(transactionRepository.findByIdAndAccountId(1L, testAccountId))
                .thenReturn(Optional.of(testTransaction));

        TransactionDto result = transactionService.getTransaction(testAccountId, 1L);

        assertEquals(1L, result.getId());
        assertEquals(testAccountId, result.getAccountId());
        assertEquals("TXN-001", result.getTxId());
        assertEquals(LocalDate.of(2026, 8, 10), result.getTxDate());
        assertEquals(0, BigDecimal.valueOf(100.50).compareTo(result.getAmount()));
        assertEquals("DEBIT", result.getDirection());
        assertEquals("Invoice Payment", result.getDescription());
        assertEquals("Supplier A", result.getCounterparty());
        assertEquals("REF-001", result.getReference());
        assertEquals(10L, result.getCategoryId());
        assertEquals("categorized", result.getStatus());
        assertEquals("User note", result.getNotes());
        assertEquals("CAMT.052", result.getImportSource());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
    }
}
