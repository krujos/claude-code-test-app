package com.example.newproject.service;

import com.example.newproject.model.Account;
import com.example.newproject.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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

    private Account validAccount;
    private Account invalidAccount;

    @BeforeEach
    void setUp() {
        validAccount = new Account();
        validAccount.setFirstName("John");
        validAccount.setLastName("Doe");
        validAccount.setPhoneNumber("(212) 456-7890");
        validAccount.setAddress("123 Main St");

        invalidAccount = new Account();
        invalidAccount.setFirstName("Jane");
        invalidAccount.setLastName("Smith");
        invalidAccount.setPhoneNumber("invalid");
        invalidAccount.setAddress("456 Oak Ave");
    }

    @Test
    void testCreateAccountWithValidPhoneNumber() {
        when(accountRepository.save(any(Account.class))).thenReturn(validAccount);

        Account created = accountService.createAccount(validAccount);

        assertNotNull(created);
        assertEquals("(212) 456-7890", created.getPhoneNumber());
        verify(accountRepository, times(1)).save(validAccount);
    }

    @Test
    void testCreateAccountWithInvalidPhoneNumber() {
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.createAccount(invalidAccount)
        );

        assertTrue(exception.getMessage().contains("Invalid phone number format"));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccountWithNullPhoneNumber() {
        Account nullPhoneAccount = new Account();
        nullPhoneAccount.setFirstName("Test");
        nullPhoneAccount.setLastName("User");
        nullPhoneAccount.setPhoneNumber(null);
        nullPhoneAccount.setAddress("789 Pine Rd");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.createAccount(nullPhoneAccount)
        );

        assertTrue(exception.getMessage().contains("Phone number is required"));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccountWithEmptyPhoneNumber() {
        Account emptyPhoneAccount = new Account();
        emptyPhoneAccount.setFirstName("Test");
        emptyPhoneAccount.setLastName("User");
        emptyPhoneAccount.setPhoneNumber("");
        emptyPhoneAccount.setAddress("789 Pine Rd");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.createAccount(emptyPhoneAccount)
        );

        assertTrue(exception.getMessage().contains("Phone number is required"));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testUpdateAccountWithValidPhoneNumber() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(validAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(validAccount);

        Account updateData = new Account();
        updateData.setFirstName("John");
        updateData.setLastName("Doe");
        updateData.setPhoneNumber("+1 415 987 6543");
        updateData.setAddress("123 Main St");

        Account updated = accountService.updateAccount(1L, updateData);

        assertNotNull(updated);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testUpdateAccountWithInvalidPhoneNumber() {
        Account updateData = new Account();
        updateData.setFirstName("John");
        updateData.setLastName("Doe");
        updateData.setPhoneNumber("abc123");
        updateData.setAddress("123 Main St");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> accountService.updateAccount(1L, updateData)
        );

        assertTrue(exception.getMessage().contains("Invalid phone number format"));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void testCreateAccountWithInternationalPhoneNumber() {
        Account internationalAccount = new Account();
        internationalAccount.setFirstName("Pierre");
        internationalAccount.setLastName("Dupont");
        internationalAccount.setPhoneNumber("+33 1 42 86 82 00");
        internationalAccount.setAddress("Paris, France");

        when(accountRepository.save(any(Account.class))).thenReturn(internationalAccount);

        Account created = accountService.createAccount(internationalAccount);

        assertNotNull(created);
        assertEquals("+33 1 42 86 82 00", created.getPhoneNumber());
        verify(accountRepository, times(1)).save(internationalAccount);
    }
}
