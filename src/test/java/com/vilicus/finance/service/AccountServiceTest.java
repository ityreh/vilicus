package com.vilicus.finance.service;

import com.vilicus.finance.dto.AccountDto;
import com.vilicus.finance.dto.CreateAccountRequest;
import com.vilicus.finance.entity.Account;
import com.vilicus.finance.entity.AccountType;
import com.vilicus.finance.exception.ResourceNotFoundException;
import com.vilicus.finance.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    private Long testUserId = 1L;
    private CreateAccountRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = new CreateAccountRequest();
        validRequest.setName("Checking Account");
        validRequest.setIban("DE89370400440532013000");
        validRequest.setType("BANK_ACCOUNT");
        validRequest.setCurrency("EUR");
    }

    @Test
    void testCreateAccount_Success() {
        // Mock repository: no duplicates, will save successfully
        when(accountRepository.existsByUserIdAndIban(testUserId, validRequest.getIban())).thenReturn(false);
        when(accountRepository.existsByUserIdAndName(testUserId, validRequest.getName())).thenReturn(false);

        Account savedAccount = Account.builder()
                .id(1L)
                .userId(testUserId)
                .iban(validRequest.getIban())
                .name(validRequest.getName())
                .type(AccountType.BANK_ACCOUNT)
                .currency(validRequest.getCurrency())
                .balance(BigDecimal.ZERO)
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        // Call service
        AccountDto result = accountService.createAccount(testUserId, validRequest);

        // Verify
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(validRequest.getName(), result.getName());
        assertEquals(validRequest.getIban(), result.getIban());
        assertEquals("BANK_ACCOUNT", result.getType());
        assertEquals("EUR", result.getCurrency());
        assertEquals(BigDecimal.ZERO, result.getBalance());
        assertEquals("active", result.getStatus());

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccount_DuplicateIban_ThrowsException() {
        // Mock: IBAN already exists for this user
        when(accountRepository.existsByUserIdAndIban(testUserId, validRequest.getIban())).thenReturn(true);

        // Call and expect exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount(testUserId, validRequest)
        );

        assertEquals("You already have an account with this IBAN", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccount_DuplicateName_ThrowsException() {
        // Mock: name exists, IBAN doesn't
        when(accountRepository.existsByUserIdAndIban(testUserId, validRequest.getIban())).thenReturn(false);
        when(accountRepository.existsByUserIdAndName(testUserId, validRequest.getName())).thenReturn(true);

        // Call and expect exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount(testUserId, validRequest)
        );

        assertEquals("You already have an account with this name", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccount_InvalidIbanFormat_ThrowsException() {
        validRequest.setIban("INVALID");

        // Call and expect exception (IBAN validation happens before DB call)
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount(testUserId, validRequest)
        );

        assertEquals("Invalid IBAN format", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccount_InvalidAccountType_ThrowsException() {
        validRequest.setType("INVALID_TYPE");

        when(accountRepository.existsByUserIdAndIban(testUserId, validRequest.getIban())).thenReturn(false);
        when(accountRepository.existsByUserIdAndName(testUserId, validRequest.getName())).thenReturn(false);

        // Call and expect exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount(testUserId, validRequest)
        );

        assertTrue(exception.getMessage().contains("Invalid account type"));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccount_InvalidCurrency_ThrowsException() {
        validRequest.setCurrency("INVALID");

        when(accountRepository.existsByUserIdAndIban(testUserId, validRequest.getIban())).thenReturn(false);
        when(accountRepository.existsByUserIdAndName(testUserId, validRequest.getName())).thenReturn(false);

        // Call and expect exception
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> accountService.createAccount(testUserId, validRequest)
        );

        assertEquals("Currency must be 3 uppercase letters (ISO 4217)", exception.getMessage());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccount_ValidatesAllAccountTypes() {
        AccountType[] types = {
                AccountType.BANK_ACCOUNT,
                AccountType.CREDIT_CARD,
                AccountType.CASH,
                AccountType.SAVINGS,
                AccountType.INVESTMENT
        };

        for (AccountType type : types) {
            validRequest.setType(type.name());
            when(accountRepository.existsByUserIdAndIban(testUserId, validRequest.getIban())).thenReturn(false);
            when(accountRepository.existsByUserIdAndName(testUserId, validRequest.getName())).thenReturn(false);

            Account savedAccount = Account.builder()
                    .id(1L)
                    .userId(testUserId)
                    .iban(validRequest.getIban())
                    .name(validRequest.getName())
                    .type(type)
                    .currency("EUR")
                    .balance(BigDecimal.ZERO)
                    .status("active")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

            AccountDto result = accountService.createAccount(testUserId, validRequest);
            assertEquals(type.name(), result.getType());
        }
    }

    @Test
    void testGetAccount_Success() {
        Account account = Account.builder()
                .id(1L)
                .userId(testUserId)
                .iban("DE89370400440532013000")
                .name("Checking")
                .type(AccountType.BANK_ACCOUNT)
                .currency("EUR")
                .balance(BigDecimal.valueOf(1000))
                .status("active")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(accountRepository.findByUserIdAndId(testUserId, 1L)).thenReturn(Optional.of(account));

        AccountDto result = accountService.getAccount(testUserId, 1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Checking", result.getName());
    }

    @Test
    void testGetAccount_NotFound_ThrowsException() {
        when(accountRepository.findByUserIdAndId(testUserId, 999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.getAccount(testUserId, 999L)
        );

        assertEquals("Account not found", exception.getMessage());
    }

    @Test
    void testGetAccount_UserMismatch_ThrowsException() {
        // Account belongs to different user
        when(accountRepository.findByUserIdAndId(testUserId, 1L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> accountService.getAccount(testUserId, 1L)
        );

        assertEquals("Account not found", exception.getMessage());
    }
}
