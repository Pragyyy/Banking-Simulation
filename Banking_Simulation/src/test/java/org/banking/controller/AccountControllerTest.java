package org.banking.controller;

import org.banking.model.Account;
import org.banking.model.ApiResponse;
import org.banking.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AccountControllerTest {

    @Mock
    private AccountService accountService;

    @InjectMocks
    private AccountController accountController;

    private Account validAccount;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        validAccount = new Account();
        validAccount.setAccountId("ACC_000001");
        validAccount.setCustomerId("CUST_000001");
        validAccount.setAadharNumber("123456789012");
        validAccount.setAccountNumber("1234567890123456");
        validAccount.setAccountName("John Doe");
        validAccount.setAccountType("SAVINGS");
        validAccount.setPhoneNumberLinked("9876543210");
        validAccount.setIfscCode("BANK0001234");
        validAccount.setBankName("Test Bank");
        validAccount.setBalance(1000.00);
        validAccount.setStatus("Active");
        validAccount.setCreatedAt(LocalDateTime.now());
        validAccount.setModifiedAt(LocalDateTime.now());
    }

    // ========== CREATE ACCOUNT TESTS ==========

    @Test
    void testCreateAccount_Success() throws Exception {
        when(accountService.isAccountNumberExists(anyString())).thenReturn(false);
        when(accountService.createAccount(any(Account.class))).thenReturn(validAccount);

        Response response = accountController.createAccount(validAccount);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertEquals("Account created successfully", apiResponse.getMessage());
        verify(accountService, times(1)).createAccount(any(Account.class));
    }

    @Test
    void testCreateAccount_MissingAadharNumber() {
        validAccount.setAadharNumber(null);

        Response response = accountController.createAccount(validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Aadhar number is required", apiResponse.getMessage());
    }

    @Test
    void testCreateAccount_EmptyAadharNumber() {
        validAccount.setAadharNumber("   ");

        Response response = accountController.createAccount(validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
    }

    @Test
    void testCreateAccount_InvalidAadharNumber() {
        validAccount.setAadharNumber("12345"); // Not 12 digits

        Response response = accountController.createAccount(validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid Aadhar"));
    }

    @Test
    void testCreateAccount_InvalidAccountNumber_TooShort() {
        validAccount.setAccountNumber("123"); // Less than 10 digits

        Response response = accountController.createAccount(validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid account number"));
    }

    @Test
    void testCreateAccount_InvalidAccountNumber_TooLong() {
        validAccount.setAccountNumber("1234567890123456789"); // More than 18 digits

        Response response = accountController.createAccount(validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void testCreateAccount_InvalidPhoneNumber() {
        validAccount.setPhoneNumberLinked("0123456789"); // Starts with 0

        Response response = accountController.createAccount(validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid phone number"));
    }

    @Test
    void testCreateAccount_InvalidAccountName() {
        validAccount.setAccountName("John123"); // Contains numbers

        Response response = accountController.createAccount(validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid account name"));
    }

    @Test
    void testCreateAccount_InvalidAccountType() {
        validAccount.setAccountType("INVALID_TYPE");

        Response response = accountController.createAccount(validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid account type"));
    }

    @Test
    void testCreateAccount_ValidAccountTypes() throws Exception {
        String[] validTypes = {"SAVINGS", "CURRENT", "FIXED", "RECURRING"};

        for (String type : validTypes) {
            validAccount.setAccountType(type);
            when(accountService.isAccountNumberExists(anyString())).thenReturn(false);
            when(accountService.createAccount(any(Account.class))).thenReturn(validAccount);

            Response response = accountController.createAccount(validAccount);

            assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        }
    }

    @Test
    void testCreateAccount_MissingIfscCode() {
        validAccount.setIfscCode(null);

        Response response = accountController.createAccount(validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("IFSC code is required", apiResponse.getMessage());
    }

    @Test
    void testCreateAccount_MissingBankName() {
        validAccount.setBankName("");

        Response response = accountController.createAccount(validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Bank name is required", apiResponse.getMessage());
    }

    @Test
    void testCreateAccount_DuplicateAccountNumber() throws Exception {
        when(accountService.isAccountNumberExists(anyString())).thenReturn(true);

        Response response = accountController.createAccount(validAccount);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Account number already exists", apiResponse.getMessage());
    }

//    @Test
//    void testCreateAccount_ServiceException() throws Exception {
//        when(accountService.isAccountNumberExists(anyString())).thenReturn(false);
//        when(accountService.createAccount(any(Account.class)))
//                .thenThrow(new Exception("Database error"));
//
//        Response response = accountController.createAccount(validAccount);
//
//        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
//        ApiResponse apiResponse = (ApiResponse) response.getEntity();
//        assertFalse(apiResponse.isSuccess());
//        assertTrue(apiResponse.getMessage().contains("Failed to create account"));
//    }

    // ========== GET ACCOUNT BY ID TESTS ==========

//    @Test
//    void testGetAccountById_Success() throws Exception {
//        when(accountService.getAccountById("ACC_000001")).thenReturn(validAccount);
//
//        Response response = accountController.getAccountById("ACC_000001");
//
//        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
//        ApiResponse apiResponse = (ApiResponse) response.getEntity();
//        assertTrue(apiResponse.isSuccess());
//        assertNotNull(apiResponse.getData());
//    }

//    @Test
//    void testGetAccountById_NotFound() throws Exception {
//        when(accountService.getAccountById("ACC_999999")).thenReturn(null);
//
//        Response response = accountController.getAccountById("ACC_999999");
//
//        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
//        ApiResponse apiResponse = (ApiResponse) response.getEntity();
//        assertFalse(apiResponse.isSuccess());
//        assertEquals("Account not found", apiResponse.getMessage());
//    }

    // ========== GET ACCOUNT BY NUMBER TESTS ==========

    @Test
    void testGetAccountByNumber_Success() throws Exception {
        when(accountService.getAccountByAccountNumber("1234567890123456")).thenReturn(validAccount);

        Response response = accountController.getAccountByNumber("1234567890123456");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
    }

    @Test
    void testGetAccountByNumber_NotFound() throws Exception {
        when(accountService.getAccountByAccountNumber("9999999999999999")).thenReturn(null);

        Response response = accountController.getAccountByNumber("9999999999999999");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    // ========== GET ACCOUNTS BY CUSTOMER ID TESTS ==========

//    @Test
//    void testGetAccountsByCustomerId_Success() throws Exception {
//        List<Account> accounts = Arrays.asList(validAccount);
//        when(accountService.getAccountsByCustomerId("CUST_000001")).thenReturn(accounts);
//
//        Response response = accountController.getAccountsByCustomerId("CUST_000001");
//
//        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
//        ApiResponse apiResponse = (ApiResponse) response.getEntity();
//        assertTrue(apiResponse.isSuccess());
//    }

//    @Test
//    void testGetAccountsByCustomerId_EmptyList() throws Exception {
//        when(accountService.getAccountsByCustomerId("CUST_999999")).thenReturn(Arrays.asList());
//
//        Response response = accountController.getAccountsByCustomerId("CUST_999999");
//
//        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
//        ApiResponse apiResponse = (ApiResponse) response.getEntity();
//        assertFalse(apiResponse.isSuccess());
//        assertEquals("No accounts found for this customer", apiResponse.getMessage());
//    }

    // ========== GET ACCOUNTS BY AADHAR TESTS ==========

    @Test
    void testGetAccountsByAadhar_Success() throws Exception {
        List<Account> accounts = Arrays.asList(validAccount);
        when(accountService.getAccountsByAadhar("123456789012")).thenReturn(accounts);

        Response response = accountController.getAccountsByAadhar("123456789012");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
    }

    @Test
    void testGetAccountsByAadhar_InvalidAadhar() {
        Response response = accountController.getAccountsByAadhar("12345");

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid Aadhar"));
    }

    @Test
    void testGetAccountsByAadhar_EmptyList() throws Exception {
        when(accountService.getAccountsByAadhar("123456789012")).thenReturn(Arrays.asList());

        Response response = accountController.getAccountsByAadhar("123456789012");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
    }

    // ========== UPDATE ACCOUNT TESTS ==========

    @Test
    void testUpdateAccount_Success() throws Exception {
        when(accountService.getAccountByAccountNumber("1234567890123456")).thenReturn(validAccount);
        when(accountService.updateAccount(anyString(), any(Account.class))).thenReturn(validAccount);

        Response response = accountController.updateAccount("1234567890123456", validAccount);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertEquals("Account updated successfully", apiResponse.getMessage());
    }

    @Test
    void testUpdateAccount_NotFound() throws Exception {
        when(accountService.getAccountById("ACC_999999")).thenReturn(null);

        Response response = accountController.updateAccount("ACC_999999", validAccount);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
    }

    @Test
    void testUpdateAccount_InvalidAccountName() throws Exception {
        when(accountService.getAccountByAccountNumber("1234567890123456")).thenReturn(validAccount);
        validAccount.setAccountName("John123");

        Response response = accountController.updateAccount("1234567890123456", validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void testUpdateAccount_InvalidPhoneNumber() throws Exception {
        when(accountService.getAccountByAccountNumber("1234567890123456")).thenReturn(validAccount);
        validAccount.setPhoneNumberLinked("123");

        Response response = accountController.updateAccount("1234567890123456", validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void testUpdateAccount_InvalidStatus() throws Exception {
        when(accountService.getAccountByAccountNumber("1234567890123456")).thenReturn(validAccount);
        validAccount.setStatus("INVALID_STATUS");

        Response response = accountController.updateAccount("1234567890123456", validAccount);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    // ========== DELETE ACCOUNT TESTS ==========

    @Test
    void testDeleteAccount_Success() throws Exception {
        when(accountService.deleteAccount("1234567890123456")).thenReturn(true);

        Response response = accountController.deleteAccount("1234567890123456");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertEquals("Account deleted successfully", apiResponse.getMessage());
    }

    @Test
    void testDeleteAccount_NotFound() throws Exception {
        when(accountService.deleteAccount("ACC_999999")).thenReturn(false);

        Response response = accountController.deleteAccount("ACC_999999");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    // ========== GET ALL ACCOUNTS TESTS ==========

    @Test
    void testGetAllAccounts_Success() throws Exception {
        List<Account> accounts = Arrays.asList(validAccount);
        when(accountService.getAllAccounts()).thenReturn(accounts);

        Response response = accountController.getAllAccounts();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertNotNull(apiResponse.getData());
    }
}