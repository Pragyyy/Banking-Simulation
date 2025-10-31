package org.banking.service;

import org.banking.model.Account;
import org.banking.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AccountServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountServiceImpl accountService;

    private Account validAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validAccount = new Account();
        validAccount.setAadharNumber("123456789123");
        validAccount.setAccountNumber("1234567890123456");
        validAccount.setAccountName("Jane Smith");
        validAccount.setAccountType("SAVINGS");
        validAccount.setPhoneNumberLinked("9123456789");
        validAccount.setIfscCode("BANK0001234");
        validAccount.setBankName("Test Bank");
    }

    // ========== CREATE ACCOUNT TESTS ==========

    @Test
    void testCreateAccount_Success() throws Exception {
        when(accountRepository.getCustomerIdByAadhar("123456789123")).thenReturn("CUST_000001");
        when(accountRepository.getNextId()).thenReturn(1);
        when(accountRepository.save(any(Account.class))).thenReturn(validAccount);

        Account result = accountService.createAccount(validAccount);

        assertNotNull(result);
        assertEquals("ACC_000001", result.getAccountId());
        assertEquals("CUST_000001", result.getCustomerId());
        assertEquals("Active", result.getStatus());
        assertEquals(50.00, result.getBalance());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getModifiedAt());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void testCreateAccount_CustomerNotFound() throws Exception {
        when(accountRepository.getCustomerIdByAadhar("999999999999")).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> {
            accountService.createAccount(validAccount);
        });

        assertTrue(exception.getMessage().contains("Customer not found with Aadhar number"));
    }

    @Test
    void testCreateAccount_GeneratesCorrectId() throws Exception {
        when(accountRepository.getCustomerIdByAadhar(anyString())).thenReturn("CUST_000001");
        when(accountRepository.getNextId()).thenReturn(25);
        when(accountRepository.save(any(Account.class))).thenReturn(validAccount);

        Account result = accountService.createAccount(validAccount);

        assertEquals("ACC_000025", result.getAccountId());
    }

    @Test
    void testCreateAccount_PreservesBalance() throws Exception {
        validAccount.setBalance(1000.00);
        when(accountRepository.getCustomerIdByAadhar(anyString())).thenReturn("CUST_000001");
        when(accountRepository.getNextId()).thenReturn(1);
        when(accountRepository.save(any(Account.class))).thenReturn(validAccount);

        Account result = accountService.createAccount(validAccount);

        assertEquals(1000.00, result.getBalance());
    }

    @Test
    void testCreateAccount_PreservesStatus() throws Exception {
        validAccount.setStatus("Inactive");
        when(accountRepository.getCustomerIdByAadhar(anyString())).thenReturn("CUST_000001");
        when(accountRepository.getNextId()).thenReturn(1);
        when(accountRepository.save(any(Account.class))).thenReturn(validAccount);

        Account result = accountService.createAccount(validAccount);

        assertEquals("Inactive", result.getStatus());
    }

    @Test
    void testCreateAccount_RepositoryException() throws Exception {
        when(accountRepository.getCustomerIdByAadhar(anyString())).thenReturn("CUST_000001");
        when(accountRepository.getNextId()).thenReturn(1);
        when(accountRepository.save(any(Account.class)))
                .thenThrow(new SQLException("Database error"));

        Exception exception = assertThrows(Exception.class, () -> {
            accountService.createAccount(validAccount);
        });

        assertTrue(exception.getMessage().contains("Failed to create account"));
    }

    // ========== GET ACCOUNT BY ID TESTS ==========

    @Test
    void testGetAccountById_Success() throws Exception {
        validAccount.setAccountId("ACC_000001");
        when(accountRepository.findById("ACC_000001")).thenReturn(validAccount);

        Account result = accountService.getAccountById("ACC_000001");

        assertNotNull(result);
        assertEquals("ACC_000001", result.getAccountId());
        verify(accountRepository, times(1)).findById("ACC_000001");
    }

    @Test
    void testGetAccountById_NotFound() throws Exception {
        when(accountRepository.findById("ACC_999999")).thenReturn(null);

        Account result = accountService.getAccountById("ACC_999999");

        assertNull(result);
    }

    @Test
    void testGetAccountById_RepositoryException() throws Exception {
        when(accountRepository.findById(anyString()))
                .thenThrow(new SQLException("Database error"));

        Exception exception = assertThrows(Exception.class, () -> {
            accountService.getAccountById("ACC_000001");
        });

        assertTrue(exception.getMessage().contains("Failed to retrieve account"));
    }

    // ========== GET ACCOUNT BY ACCOUNT NUMBER TESTS ==========

    @Test
    void testGetAccountByAccountNumber_Success() throws Exception {
        when(accountRepository.findByAccountNumber("1234567890123456")).thenReturn(validAccount);

        Account result = accountService.getAccountByAccountNumber("1234567890123456");

        assertNotNull(result);
        assertEquals("1234567890123456", result.getAccountNumber());
        verify(accountRepository, times(1)).findByAccountNumber("1234567890123456");
    }

    @Test
    void testGetAccountByAccountNumber_NotFound() throws Exception {
        when(accountRepository.findByAccountNumber("9999999999999999")).thenReturn(null);

        Account result = accountService.getAccountByAccountNumber("9999999999999999");

        assertNull(result);
    }

    // ========== GET ACCOUNTS BY CUSTOMER ID TESTS ==========

    @Test
    void testGetAccountsByCustomerId_Success() throws Exception {
        Account account2 = new Account();
        account2.setAccountId("ACC_000002");

        List<Account> accounts = Arrays.asList(validAccount, account2);
        when(accountRepository.findByCustomerId("CUST_000001")).thenReturn(accounts);

        List<Account> result = accountService.getAccountsByCustomerId("CUST_000001");

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(accountRepository, times(1)).findByCustomerId("CUST_000001");
    }

    @Test
    void testGetAccountsByCustomerId_EmptyList() throws Exception {
        when(accountRepository.findByCustomerId("CUST_999999")).thenReturn(Arrays.asList());

        List<Account> result = accountService.getAccountsByCustomerId("CUST_999999");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== GET ACCOUNTS BY AADHAR TESTS ==========

    @Test
    void testGetAccountsByAadhar_Success() throws Exception {
        when(accountRepository.getCustomerIdByAadhar("123456789012")).thenReturn("CUST_000001");
        when(accountRepository.findByCustomerId("CUST_000001")).thenReturn(Arrays.asList(validAccount));

        List<Account> result = accountService.getAccountsByAadhar("123456789012");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(accountRepository, times(1)).getCustomerIdByAadhar("123456789012");
        verify(accountRepository, times(1)).findByCustomerId("CUST_000001");
    }

    @Test
    void testGetAccountsByAadhar_CustomerNotFound() throws Exception {
        when(accountRepository.getCustomerIdByAadhar("999999999999")).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> {
            accountService.getAccountsByAadhar("999999999999");
        });

        assertTrue(exception.getMessage().contains("Customer not found with Aadhar number"));
    }

    @Test
    void testGetAccountsByAadhar_RepositoryException() throws Exception {
        when(accountRepository.getCustomerIdByAadhar(anyString()))
                .thenThrow(new SQLException("Database error"));

        Exception exception = assertThrows(Exception.class, () -> {
            accountService.getAccountsByAadhar("123456789012");
        });

        assertTrue(exception.getMessage().contains("Failed to retrieve accounts"));
    }

    // ========== UPDATE ACCOUNT TESTS ==========

    @Test
    void testUpdateAccount_Success() throws Exception {
        validAccount.setAccountId("ACC_000001");
        when(accountRepository.findByAccountNumber("1234567890123456")).thenReturn(validAccount);
        when(accountRepository.update("1234567890123456", validAccount)).thenReturn(validAccount);

        Account result = accountService.updateAccount("1234567890123456", validAccount);

        assertNotNull(result);
        assertEquals("ACC_000001", result.getAccountId());
        verify(accountRepository, times(1)).update("1234567890123456", validAccount);
    }

    @Test
    void testUpdateAccount_AccountNotFoundBeforeUpdate() throws Exception {
        when(accountRepository.findById("ACC_999999")).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> {
            accountService.updateAccount("9999999999999999", validAccount);
        });

        assertTrue(exception.getMessage().contains("Account not found with Account Number"));
    }

    @Test
    void testUpdateAccount_UpdateFailed() throws Exception {
        validAccount.setAccountId("ACC_000001");
        when(accountRepository.findById("ACC_000001")).thenReturn(validAccount);
        when(accountRepository.update("1234567890123456", validAccount)).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> {
            accountService.updateAccount("1234567890123456", validAccount);
        });

        assertTrue(exception.getMessage().contains("Failed to update account"));
    }

    @Test
    void testUpdateAccount_RepositoryException() throws Exception {
        when(accountRepository.findById("ACC_000001")).thenReturn(validAccount);
        when(accountRepository.update(anyString(), any(Account.class)))
                .thenThrow(new SQLException("Update failed"));

        Exception exception = assertThrows(Exception.class, () -> {
            accountService.updateAccount("1234567890123456", validAccount);
        });

        assertTrue(exception.getMessage().contains("Failed to update account"));
    }

    // ========== DELETE ACCOUNT TESTS ==========

    @Test
    void testDeleteAccount_Success() throws Exception {
        when(accountRepository.deleteByAccountNumber("1234567890123456")).thenReturn(true);

        boolean result = accountService.deleteAccount("1234567890123456");

        assertTrue(result);
        verify(accountRepository, times(1)).deleteByAccountNumber("1234567890123456");
    }

    @Test
    void testDeleteAccount_NotFound() throws Exception {
        when(accountRepository.deleteByAccountNumber("ACC_999999")).thenReturn(false);

        boolean result = accountService.deleteAccount("9999999999999999");

        assertFalse(result);
    }

    @Test
    void testDeleteAccount_RepositoryException() throws Exception {
        when(accountRepository.deleteByAccountNumber(anyString()))
                .thenThrow(new SQLException("Delete failed"));

        Exception exception = assertThrows(Exception.class, () -> {
            accountService.deleteAccount("1234567890123456");
        });

        assertTrue(exception.getMessage().contains("Failed to delete account"));
    }

    // ========== GET ALL ACCOUNTS TESTS ==========

    @Test
    void testGetAllAccounts_Success() throws Exception {
        Account account2 = new Account();
        account2.setAccountId("ACC_000002");

        List<Account> accounts = Arrays.asList(validAccount, account2);
        when(accountRepository.findAll()).thenReturn(accounts);

        List<Account> result = accountService.getAllAccounts();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(accountRepository, times(1)).findAll();
    }

    @Test
    void testGetAllAccounts_EmptyList() throws Exception {
        when(accountRepository.findAll()).thenReturn(Arrays.asList());

        List<Account> result = accountService.getAllAccounts();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetAllAccounts_RepositoryException() throws Exception {
        when(accountRepository.findAll()).thenThrow(new SQLException("Database error"));

        Exception exception = assertThrows(Exception.class, () -> {
            accountService.getAllAccounts();
        });

        assertTrue(exception.getMessage().contains("Failed to retrieve accounts"));
    }

    // ========== ACCOUNT NUMBER EXISTS TESTS ==========

    @Test
    void testIsAccountNumberExists_True() throws Exception {
        when(accountRepository.existsByAccountNumber("1234567890123456")).thenReturn(true);

        boolean result = accountService.isAccountNumberExists("1234567890123456");

        assertTrue(result);
    }

    @Test
    void testIsAccountNumberExists_False() throws Exception {
        when(accountRepository.existsByAccountNumber("9999999999999999")).thenReturn(false);

        boolean result = accountService.isAccountNumberExists("9999999999999999");

        assertFalse(result);
    }

    @Test
    void testIsAccountNumberExists_Exception() throws Exception {
        when(accountRepository.existsByAccountNumber(anyString()))
                .thenThrow(new SQLException("Database error"));

        Exception exception = assertThrows(Exception.class, () -> {
            accountService.isAccountNumberExists("1234567890123456");
        });

        assertTrue(exception.getMessage().contains("Failed to check account number"));
    }
}