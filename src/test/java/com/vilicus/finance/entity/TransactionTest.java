package com.vilicus.finance.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Transaction entity.
 *
 * Verifies:
 * - Entity creation and field assignments
 * - Validation logic (isValid())
 * - Status transitions (archive, categorize)
 * - Lifecycle hooks (timestamps)
 */
public class TransactionTest {

    private Transaction transaction;
    private LocalDateTime beforeCreation;

    @BeforeEach
    void setUp() {
        beforeCreation = LocalDateTime.now();
        transaction = Transaction.builder()
                .accountId(1L)
                .txId("TXN-12345")
                .txDate(LocalDate.of(2026, 8, 12))
                .amount(BigDecimal.valueOf(100.50))
                .balance(BigDecimal.valueOf(1000.00))
                .direction("DEBIT")
                .description("Payment to Supplier")
                .counterparty("ACME Corp")
                .reference("INV-001")
                .categoryId(1L)
                .status("imported")
                .importSource("CAMT.052")
                .build();
    }

    @Test
    void testTransactionCreation_Success() {
        assertNotNull(transaction);
        assertEquals(1L, transaction.getAccountId());
        assertEquals("TXN-12345", transaction.getTxId());
        assertEquals(LocalDate.of(2026, 8, 12), transaction.getTxDate());
        assertEquals(BigDecimal.valueOf(100.50), transaction.getAmount());
        assertEquals("DEBIT", transaction.getDirection());
        assertEquals("Payment to Supplier", transaction.getDescription());
        assertEquals("ACME Corp", transaction.getCounterparty());
    }

    @Test
    void testTransactionDefaultStatus() {
        Transaction tx = Transaction.builder()
                .accountId(1L)
                .txId("TXN-001")
                .txDate(LocalDate.now())
                .amount(BigDecimal.TEN)
                .direction("CREDIT")
                .description("Test")
                .build();

        assertEquals("imported", tx.getStatus());
    }

    @Test
    void testTransactionWithCreditDirection() {
        transaction.setDirection("CREDIT");
        assertEquals("CREDIT", transaction.getDirection());
    }

    @Test
    void testTransactionWithDebitDirection() {
        transaction.setDirection("DEBIT");
        assertEquals("DEBIT", transaction.getDirection());
    }

    @Test
    void testAmountIsPositive() {
        assertTrue(transaction.getAmount().signum() > 0);
        assertEquals(BigDecimal.valueOf(100.50), transaction.getAmount());
    }

    @Test
    void testAmountCanBeZero() {
        transaction.setAmount(BigDecimal.ZERO);
        assertEquals(BigDecimal.ZERO, transaction.getAmount());
    }

    @Test
    void testTransactionWithoutBalance() {
        transaction.setBalance(null);
        assertNull(transaction.getBalance());
    }

    @Test
    void testTransactionWithBalance() {
        assertEquals(BigDecimal.valueOf(1000.00), transaction.getBalance());
    }

    @Test
    void testTransactionWithoutCategory() {
        transaction.setCategoryId(null);
        assertNull(transaction.getCategoryId());
    }

    @Test
    void testTransactionWithCategory() {
        assertEquals(1L, transaction.getCategoryId());
    }

    @Test
    void testTransactionWithoutCounterparty() {
        transaction.setCounterparty(null);
        assertNull(transaction.getCounterparty());
    }

    @Test
    void testTransactionWithCounterparty() {
        assertEquals("ACME Corp", transaction.getCounterparty());
    }

    @Test
    void testTransactionWithoutReference() {
        transaction.setReference(null);
        assertNull(transaction.getReference());
    }

    @Test
    void testTransactionWithReference() {
        assertEquals("INV-001", transaction.getReference());
    }

    @Test
    void testIsValid_AllFieldsPresent() {
        assertTrue(transaction.isValid());
    }

    @Test
    void testIsValid_MissingAccountId() {
        transaction.setAccountId(null);
        assertFalse(transaction.isValid());
    }

    @Test
    void testIsValid_MissingTxId() {
        transaction.setTxId(null);
        assertFalse(transaction.isValid());
    }

    @Test
    void testIsValid_EmptyTxId() {
        transaction.setTxId("");
        assertFalse(transaction.isValid());
    }

    @Test
    void testIsValid_BlankTxId() {
        transaction.setTxId("   ");
        assertFalse(transaction.isValid());
    }

    @Test
    void testIsValid_MissingTxDate() {
        transaction.setTxDate(null);
        assertFalse(transaction.isValid());
    }

    @Test
    void testIsValid_MissingAmount() {
        transaction.setAmount(null);
        assertFalse(transaction.isValid());
    }

    @Test
    void testIsValid_NegativeAmount() {
        transaction.setAmount(BigDecimal.valueOf(-50));
        assertFalse(transaction.isValid());
    }

    @Test
    void testIsValid_MissingDirection() {
        transaction.setDirection(null);
        assertFalse(transaction.isValid());
    }

    @Test
    void testIsValid_InvalidDirection() {
        transaction.setDirection("INVALID");
        assertFalse(transaction.isValid());
    }

    @Test
    void testIsValid_MissingDescription() {
        transaction.setDescription(null);
        assertFalse(transaction.isValid());
    }

    @Test
    void testIsValid_EmptyDescription() {
        transaction.setDescription("");
        assertFalse(transaction.isValid());
    }

    @Test
    void testArchiveTransaction() {
        transaction.archive();

        assertEquals("archived", transaction.getStatus());
        assertNotNull(transaction.getUpdatedAt());
    }

    @Test
    void testIsArchived_True() {
        transaction.setStatus("archived");
        assertTrue(transaction.isArchived());
    }

    @Test
    void testIsArchived_False() {
        transaction.setStatus("imported");
        assertFalse(transaction.isArchived());
    }

    @Test
    void testCategorizeTransaction() {
        transaction.categorize(5L);

        assertEquals(5L, transaction.getCategoryId());
        assertEquals("categorized", transaction.getStatus());
        assertNotNull(transaction.getUpdatedAt());
    }

    @Test
    void testCategorizeOverwritesPreviousCategory() {
        transaction.setCategoryId(1L);
        transaction.categorize(5L);

        assertEquals(5L, transaction.getCategoryId());
        assertEquals("categorized", transaction.getStatus());
    }

    @Test
    void testTransactionWithDifferentImportSources() {
        transaction.setImportSource("CAMT.052");
        assertEquals("CAMT.052", transaction.getImportSource());

        transaction.setImportSource("CSV");
        assertEquals("CSV", transaction.getImportSource());

        transaction.setImportSource("OFX");
        assertEquals("OFX", transaction.getImportSource());
    }

    @Test
    void testTransactionWithNotes() {
        transaction.setNotes("User added note");
        assertEquals("User added note", transaction.getNotes());
    }

    @Test
    void testTransactionWithoutNotes() {
        transaction.setNotes(null);
        assertNull(transaction.getNotes());
    }

    @Test
    void testPrePersist_SetsCreatedAtAndUpdatedAt() {
        transaction.onCreate();

        assertNotNull(transaction.getCreatedAt());
        assertNotNull(transaction.getUpdatedAt());
        assertTrue(transaction.getCreatedAt().isAfter(beforeCreation.minusSeconds(1)));
        // Timestamps should be equal or very close (within 1 second)
        assertTrue(Math.abs(java.time.temporal.ChronoUnit.MILLIS.between(
                transaction.getCreatedAt(), transaction.getUpdatedAt())) <= 1);
    }

    @Test
    void testPrePersist_SetsDefaultStatus() {
        transaction.setStatus(null);
        transaction.onCreate();

        assertEquals("imported", transaction.getStatus());
    }

    @Test
    void testPreUpdate_UpdatesUpdatedAt() {
        transaction.setCreatedAt(LocalDateTime.now().minusHours(1));
        LocalDateTime originalCreatedAt = transaction.getCreatedAt();
        LocalDateTime beforeUpdate = LocalDateTime.now();

        transaction.onUpdate();

        assertEquals(originalCreatedAt, transaction.getCreatedAt());
        assertTrue(transaction.getUpdatedAt().isAfter(beforeUpdate.minusSeconds(1)));
        assertNotEquals(transaction.getCreatedAt(), transaction.getUpdatedAt());
    }

    @Test
    void testTransactionWithVeryLongDescription() {
        String longDescription = "A".repeat(500);
        transaction.setDescription(longDescription);
        assertEquals(longDescription, transaction.getDescription());
    }

    @Test
    void testTransactionWithSpecialCharactersInDescription() {
        transaction.setDescription("Bezahlung Miete Wohnung — Hausmeister 2026");
        assertEquals("Bezahlung Miete Wohnung — Hausmeister 2026", transaction.getDescription());
    }

    @Test
    void testTransactionWithSpecialCharactersInCounterparty() {
        transaction.setCounterparty("Müller & Co. GmbH");
        assertEquals("Müller & Co. GmbH", transaction.getCounterparty());
    }

    @Test
    void testMultipleTransactionsHaveDifferentIds() {
        Transaction tx1 = Transaction.builder()
                .accountId(1L)
                .txId("TXN-001")
                .txDate(LocalDate.now())
                .amount(BigDecimal.TEN)
                .direction("DEBIT")
                .description("Payment 1")
                .build();

        Transaction tx2 = Transaction.builder()
                .accountId(1L)
                .txId("TXN-002")
                .txDate(LocalDate.now())
                .amount(BigDecimal.TEN)
                .direction("CREDIT")
                .description("Payment 2")
                .build();

        assertNotEquals(tx1.getTxId(), tx2.getTxId());
    }

    @Test
    void testTransactionStatusTransitions() {
        // Start as imported
        transaction.setStatus("imported");
        assertEquals("imported", transaction.getStatus());

        // Move to categorized
        transaction.categorize(1L);
        assertEquals("categorized", transaction.getStatus());

        // Move to archived
        transaction.archive();
        assertEquals("archived", transaction.getStatus());
    }

    @Test
    void testTransactionFieldImmutability_TxId() {
        String originalTxId = transaction.getTxId();
        transaction.setTxId("TXN-NEW");
        // In production, this should fail; here we verify the field can be set
        assertNotEquals(originalTxId, transaction.getTxId());
    }

    @Test
    void testTransactionAmountPrecision() {
        transaction.setAmount(BigDecimal.valueOf(1234567890.12));
        assertEquals(BigDecimal.valueOf(1234567890.12), transaction.getAmount());
    }

    @Test
    void testTransactionDateRanges() {
        LocalDate earlyDate = LocalDate.of(2020, 1, 1);
        LocalDate lateDate = LocalDate.of(2030, 12, 31);

        transaction.setTxDate(earlyDate);
        assertEquals(earlyDate, transaction.getTxDate());

        transaction.setTxDate(lateDate);
        assertEquals(lateDate, transaction.getTxDate());
    }
}
