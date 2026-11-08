package com.vilicus.finance.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AccountTest {

    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(1L)
                .userId(1L)
                .iban("DE89370400440532013000")
                .name("Checking Account")
                .type(AccountType.BANK_ACCOUNT)
                .currency("EUR")
                .balance(BigDecimal.valueOf(1000.00))
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void testAccountCreation() {
        assertNotNull(account);
        assertEquals(1L, account.getId());
        assertEquals(1L, account.getUserId());
        assertEquals("DE89370400440532013000", account.getIban());
        assertEquals("Checking Account", account.getName());
        assertEquals(AccountType.BANK_ACCOUNT, account.getType());
        assertEquals("EUR", account.getCurrency());
        assertEquals(BigDecimal.valueOf(1000.00), account.getBalance());
        assertEquals("active", account.getStatus());
    }

    @Test
    void testAccountBuilder() {
        Account builtAccount = Account.builder()
                .userId(2L)
                .iban("FR1420041010050500013M026")
                .name("Savings Account")
                .type(AccountType.SAVINGS)
                .currency("EUR")
                .balance(BigDecimal.valueOf(5000.00))
                .build();

        assertNotNull(builtAccount);
        assertEquals(2L, builtAccount.getUserId());
        assertEquals("FR1420041010050500013M026", builtAccount.getIban());
        assertEquals("Savings Account", builtAccount.getName());
        assertEquals(AccountType.SAVINGS, builtAccount.getType());
        assertEquals("EUR", builtAccount.getCurrency());
        assertEquals(BigDecimal.valueOf(5000.00), builtAccount.getBalance());
        assertEquals("active", builtAccount.getStatus()); // Default from builder
    }

    @Test
    void testIbanValidation_Valid() {
        account.setIban("DE89370400440532013000");
        assertTrue(account.isValidIban(), "Valid German IBAN should pass validation");

        account.setIban("FR1420041010050500013M026");
        assertTrue(account.isValidIban(), "Valid French IBAN should pass validation");

        account.setIban("GB82WEST12345698765432");
        assertTrue(account.isValidIban(), "Valid British IBAN should pass validation");
    }

    @Test
    void testIbanValidation_Invalid() {
        account.setIban("INVALID");
        assertFalse(account.isValidIban(), "Too short IBAN should fail");

        account.setIban("1234567890123456789012345678901234567890"); // 40 chars
        assertFalse(account.isValidIban(), "Too long IBAN should fail");

        account.setIban("DE123456789");
        assertFalse(account.isValidIban(), "Invalid format should fail");

        account.setIban("de89370400440532013000"); // lowercase
        assertFalse(account.isValidIban(), "Lowercase IBAN should fail");

        account.setIban(null);
        assertFalse(account.isValidIban(), "Null IBAN should fail");
    }

    @Test
    void testIbanValidation_EdgeCases() {
        // Minimum length (15 chars)
        account.setIban("ES9121000418450200051332");
        assertTrue(account.isValidIban(), "Spanish IBAN with 24 chars should pass");

        // Maximum length (34 chars)
        StringBuilder longIban = new StringBuilder("DE89");
        for (int i = 0; i < 30; i++) {
            longIban.append("A");
        }
        account.setIban(longIban.toString());
        assertTrue(account.isValidIban(), "IBAN with max length should pass");
    }

    @Test
    void testIsActive() {
        account.setStatus("active");
        assertTrue(account.isActive(), "Active account should return true");

        account.setStatus("ACTIVE");
        assertTrue(account.isActive(), "ACTIVE (uppercase) should return true");

        account.setStatus("archived");
        assertFalse(account.isActive(), "Archived account should return false");

        account.setStatus("closed");
        assertFalse(account.isActive(), "Closed account should return false");
    }

    @Test
    void testArchiveAccount() {
        account.setStatus("active");
        assertTrue(account.isActive());

        LocalDateTime beforeArchive = account.getUpdatedAt();
        account.archive();

        assertEquals("archived", account.getStatus());
        assertFalse(account.isActive());
        assertTrue(account.getUpdatedAt().isAfter(beforeArchive) ||
                   account.getUpdatedAt().isEqual(beforeArchive),
                "updatedAt should be set to now or after");
    }

    @Test
    void testAccountBalanceDefaultsToZero() {
        Account newAccount = Account.builder()
                .userId(3L)
                .iban("IT60X0542811101000000123456")
                .name("New Account")
                .type(AccountType.CREDIT_CARD)
                .currency("EUR")
                .build();

        // Note: balance is NOT auto-defaulting in @Builder, so this tests that
        // a developer must explicitly set it OR the schema default applies
        assertNull(newAccount.getBalance(), "Builder should not auto-set balance");
    }

    @Test
    void testAccountStatusDefaultsToActive() {
        Account newAccount = Account.builder()
                .userId(4L)
                .iban("CH9300762011623852957")
                .name("Swiss Account")
                .type(AccountType.SAVINGS)
                .currency("CHF")
                .balance(BigDecimal.valueOf(2500.00))
                .build();

        assertEquals("active", newAccount.getStatus(),
                     "Default status should be 'active' from @Builder.Default");
    }

    @Test
    void testAccountTypeEnum() {
        account.setType(AccountType.CREDIT_CARD);
        assertEquals(AccountType.CREDIT_CARD, account.getType());
        assertEquals("Credit Card", AccountType.CREDIT_CARD.getDisplayName());

        account.setType(AccountType.CASH);
        assertEquals(AccountType.CASH, account.getType());
        assertEquals("Cash", AccountType.CASH.getDisplayName());
    }

    @Test
    void testAccountCurrencySupportsIso4217() {
        account.setCurrency("USD");
        assertEquals("USD", account.getCurrency());

        account.setCurrency("GBP");
        assertEquals("GBP", account.getCurrency());

        account.setCurrency("JPY");
        assertEquals("JPY", account.getCurrency());
    }

    @Test
    void testAccountFieldsNotNull() {
        assertNotNull(account.getUserId());
        assertNotNull(account.getIban());
        assertNotNull(account.getName());
        assertNotNull(account.getType());
        assertNotNull(account.getCurrency());
        assertNotNull(account.getBalance());
        assertNotNull(account.getStatus());
        assertNotNull(account.getCreatedAt());
        assertNotNull(account.getUpdatedAt());
    }

    @Test
    void testAccountEquality() {
        Account account2 = Account.builder()
                .id(1L)
                .userId(1L)
                .iban("DE89370400440532013000")
                .name("Checking Account")
                .type(AccountType.BANK_ACCOUNT)
                .currency("EUR")
                .balance(BigDecimal.valueOf(1000.00))
                .status("active")
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();

        // With @Data and same field values, equality should work
        // (Note: @Data generates equals() based on all fields)
        assertEquals(account, account2);
    }

    @Test
    void testAccountToString() {
        String accountStr = account.toString();
        assertNotNull(accountStr);
        assertTrue(accountStr.contains("DE89370400440532013000"));
        assertTrue(accountStr.contains("Checking Account"));
    }
}
