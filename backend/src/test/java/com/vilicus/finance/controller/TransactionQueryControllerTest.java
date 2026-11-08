package com.vilicus.finance.controller;

import com.vilicus.finance.dto.TransactionDto;
import com.vilicus.finance.exception.ResourceNotFoundException;
import com.vilicus.finance.service.TransactionService;
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
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TransactionQueryController.
 *
 * Tests:
 * - List transactions with pagination
 * - Filter by date range
 * - Filter by category
 * - Get single transaction
 * - Get transaction statistics
 * - Sorting options
 * - Error handling
 */
@ExtendWith(MockitoExtension.class)
public class TransactionQueryControllerTest {

    @Mock(lenient = true)
    private TransactionService transactionService;

    @InjectMocks
    private TransactionQueryController controller;

    private Authentication testAuth;
    private Long testAccountId = 1L;
    private List<TransactionDto> sampleTransactions;

    @BeforeEach
    void setUp() {
        testAuth = new TestingAuthenticationToken("user", "password");

        sampleTransactions = List.of(
                TransactionDto.builder()
                        .id(1L)
                        .accountId(testAccountId)
                        .txId("TXN-001")
                        .txDate(LocalDate.of(2026, 8, 10))
                        .amount(BigDecimal.valueOf(100.50))
                        .direction("DEBIT")
                        .description("Invoice 001")
                        .counterparty("Supplier A")
                        .status("imported")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build(),
                TransactionDto.builder()
                        .id(2L)
                        .accountId(testAccountId)
                        .txId("TXN-002")
                        .txDate(LocalDate.of(2026, 8, 15))
                        .amount(BigDecimal.valueOf(250.75))
                        .direction("CREDIT")
                        .description("Payment received")
                        .counterparty("Client B")
                        .status("categorized")
                        .createdAt(LocalDateTime.now())
                        .updatedAt(LocalDateTime.now())
                        .build()
        );
    }

    @Test
    void testListTransactions_Success() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<TransactionDto> page = new PageImpl<>(sampleTransactions, pageable, 2);

        doReturn(page).when(transactionService).listTransactions(eq(testAccountId), any(Pageable.class));

        var response = controller.listTransactions(testAuth, testAccountId, 0, 20, "txDate,desc");

        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().getContent().size());
        assertEquals("TXN-001", response.getBody().getContent().get(0).getTxId());
    }

    @Test
    void testListTransactions_EmptyResult() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<TransactionDto> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        doReturn(emptyPage).when(transactionService).listTransactions(eq(testAccountId), any(Pageable.class));

        var response = controller.listTransactions(testAuth, testAccountId, 0, 20, "txDate,desc");

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().getContent().isEmpty());
    }

    @Test
    void testListTransactions_WithPagination() {
        int page = 2;
        int size = 50;
        Pageable pageable = PageRequest.of(page, size);
        Page<TransactionDto> mockPage = new PageImpl<>(sampleTransactions, pageable, 500);

        doReturn(mockPage).when(transactionService).listTransactions(eq(testAccountId), any(Pageable.class));

        var response = controller.listTransactions(testAuth, testAccountId, page, size, "txDate,desc");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().getContent().size());
    }

    @Test
    void testListTransactions_SortAscending() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<TransactionDto> page = new PageImpl<>(sampleTransactions, pageable, 2);

        doReturn(page).when(transactionService).listTransactions(eq(testAccountId), any(Pageable.class));

        // Test with ascending sort
        var response = controller.listTransactions(testAuth, testAccountId, 0, 20, "amount,asc");

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
    }

    @Test
    void testFilterByDateRange_Success() {
        LocalDate dateFrom = LocalDate.of(2026, 8, 1);
        LocalDate dateTo = LocalDate.of(2026, 8, 31);

        when(transactionService.listTransactionsByDateRange(testAccountId, dateFrom, dateTo))
                .thenReturn(sampleTransactions);

        var response = controller.filterByDateRange(testAuth, testAccountId, dateFrom, dateTo, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().size());
        assertTrue(response.getBody().get(0).getTxDate().isAfter(dateFrom.minusDays(1)));
    }

    @Test
    void testFilterByDateRange_Empty() {
        LocalDate dateFrom = LocalDate.of(2026, 9, 1);
        LocalDate dateTo = LocalDate.of(2026, 9, 30);

        when(transactionService.listTransactionsByDateRange(testAccountId, dateFrom, dateTo))
                .thenReturn(List.of());

        var response = controller.filterByDateRange(testAuth, testAccountId, dateFrom, dateTo, null);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testFilterByDateRangeAndCategory() {
        LocalDate dateFrom = LocalDate.of(2026, 8, 1);
        LocalDate dateTo = LocalDate.of(2026, 8, 31);
        Long categoryId = 5L;

        List<TransactionDto> categoryTransactions = List.of(sampleTransactions.get(0));
        categoryTransactions.get(0).setCategoryId(categoryId);

        when(transactionService.listTransactionsByCategory(testAccountId, categoryId))
                .thenReturn(categoryTransactions);

        var response = controller.filterByDateRange(testAuth, testAccountId, dateFrom, dateTo, categoryId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(categoryId, response.getBody().get(0).getCategoryId());
    }

    @Test
    void testGetByCategory_Success() {
        Long categoryId = 5L;
        List<TransactionDto> categoryTransactions = List.of(sampleTransactions.get(0));
        categoryTransactions.get(0).setCategoryId(categoryId);

        when(transactionService.listTransactionsByCategory(testAccountId, categoryId))
                .thenReturn(categoryTransactions);

        var response = controller.getByCategory(testAuth, testAccountId, categoryId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(categoryId, response.getBody().get(0).getCategoryId());
    }

    @Test
    void testGetByCategory_Empty() {
        Long categoryId = 5L;

        when(transactionService.listTransactionsByCategory(testAccountId, categoryId))
                .thenReturn(List.of());

        var response = controller.getByCategory(testAuth, testAccountId, categoryId);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetTransaction_Success() {
        Long transactionId = 1L;

        when(transactionService.getTransaction(testAccountId, transactionId))
                .thenReturn(sampleTransactions.get(0));

        var response = controller.getTransaction(testAuth, testAccountId, transactionId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("TXN-001", response.getBody().getTxId());
        assertEquals(testAccountId, response.getBody().getAccountId());
    }

    @Test
    void testGetTransaction_NotFound() {
        Long transactionId = 999L;

        when(transactionService.getTransaction(testAccountId, transactionId))
                .thenThrow(new ResourceNotFoundException("Transaction not found"));

        assertThrows(ResourceNotFoundException.class, () -> {
            controller.getTransaction(testAuth, testAccountId, transactionId);
        });
    }

    @Test
    void testGetStats_Success() {
        when(transactionService.countByStatus(testAccountId, "imported"))
                .thenReturn(45L);
        when(transactionService.countByStatus(testAccountId, "categorized"))
                .thenReturn(125L);
        when(transactionService.countByStatus(testAccountId, "archived"))
                .thenReturn(5L);

        var response = controller.getStats(testAuth, testAccountId);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(45L, response.getBody().getImported());
        assertEquals(125L, response.getBody().getCategorized());
        assertEquals(5L, response.getBody().getArchived());
        assertEquals(175L, response.getBody().getTotal());
    }

    @Test
    void testGetStats_AllZero() {
        when(transactionService.countByStatus(testAccountId, "imported"))
                .thenReturn(0L);
        when(transactionService.countByStatus(testAccountId, "categorized"))
                .thenReturn(0L);
        when(transactionService.countByStatus(testAccountId, "archived"))
                .thenReturn(0L);

        var response = controller.getStats(testAuth, testAccountId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(0L, response.getBody().getTotal());
    }

    @Test
    void testGetStats_MixedStatuses() {
        when(transactionService.countByStatus(testAccountId, "imported"))
                .thenReturn(10L);
        when(transactionService.countByStatus(testAccountId, "categorized"))
                .thenReturn(50L);
        when(transactionService.countByStatus(testAccountId, "archived"))
                .thenReturn(2L);

        var response = controller.getStats(testAuth, testAccountId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(62L, response.getBody().getTotal());
    }

    @Test
    void testListTransactions_DefaultPagination() {
        // Default: page=0, size=20
        Pageable pageable = PageRequest.of(0, 20);
        Page<TransactionDto> page = new PageImpl<>(sampleTransactions, pageable, 2);

        doReturn(page).when(transactionService).listTransactions(eq(testAccountId), any(Pageable.class));

        // Call with defaults
        var response = controller.listTransactions(testAuth, testAccountId, 0, 20, "txDate,desc");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(2, response.getBody().getContent().size());
    }

    @Test
    void testListTransactions_LargePageSize() {
        Pageable pageable = PageRequest.of(0, 1000);
        List<TransactionDto> largeList = new java.util.ArrayList<>();
        for (int i = 0; i < 500; i++) {
            largeList.add(sampleTransactions.get(i % sampleTransactions.size()));
        }
        Page<TransactionDto> page = new PageImpl<>(largeList, pageable, 500);

        doReturn(page).when(transactionService).listTransactions(eq(testAccountId), any(Pageable.class));

        var response = controller.listTransactions(testAuth, testAccountId, 0, 1000, "txDate,desc");

        assertEquals(200, response.getStatusCode().value());
        assertEquals(500, response.getBody().getContent().size());
    }

    @Test
    void testFilterByDateRange_SingleDayRange() {
        LocalDate singleDay = LocalDate.of(2026, 8, 10);

        when(transactionService.listTransactionsByDateRange(testAccountId, singleDay, singleDay))
                .thenReturn(List.of(sampleTransactions.get(0)));

        var response = controller.filterByDateRange(testAuth, testAccountId, singleDay, singleDay, null);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
        assertEquals(LocalDate.of(2026, 8, 10), response.getBody().get(0).getTxDate());
    }
}
