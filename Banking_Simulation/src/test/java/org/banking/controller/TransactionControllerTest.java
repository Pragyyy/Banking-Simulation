package org.banking.controller;

import jakarta.ws.rs.core.Response;
import org.banking.model.ApiResponse;
import org.banking.model.Transaction;
import org.banking.service.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        transactionController = new TransactionController(transactionService);
    }

    // Helper method to create mock transaction
    private Transaction createMockTransaction() {
        Transaction transaction = new Transaction();
        transaction.setTransactionId("TXN_000001");
        transaction.setAccountId("ACC_000001");
        transaction.setTransactionAmount(100.0);
        transaction.setTransactionType("DEBITED");
        transaction.setTransactionTime(LocalDateTime.now());
        transaction.setTransactionMode("UPI");
        transaction.setSenderAccountNumber("1234567890");
        transaction.setReceiverAccountNumber("9876543210");
        transaction.setDescription("Test payment");
        return transaction;
    }

    // ==================== PROCESS TRANSACTION WITH PIN TESTS ====================

    @Test
    void testProcessTransaction_Success() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "1234567890");
        requestBody.put("senderPin", "123456");
        requestBody.put("receiverAccountNumber", "9876543210");
        requestBody.put("amount", 100.0);
        requestBody.put("transactionMode", "UPI");
        requestBody.put("description", "Test payment");

        Transaction mockTransaction = createMockTransaction();
        when(transactionService.processTransactionWithPin(
                anyString(), anyString(), anyString(), anyDouble(), anyString(), anyString()))
                .thenReturn(mockTransaction);

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Transaction processed successfully"));
        assertNotNull(apiResponse.getData());

        verify(transactionService, times(1)).processTransactionWithPin(
                eq("1234567890"),
                eq("123456"),
                eq("9876543210"),
                eq(100.0),
                eq("UPI"),
                eq("Test payment")
        );
    }

    @Test
    void testProcessTransaction_MissingSenderPin() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "1234567890");
        requestBody.put("receiverAccountNumber", "9876543210");
        requestBody.put("amount", 100.0);
        requestBody.put("transactionMode", "UPI");

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Sender PIN is required", apiResponse.getMessage());

        verify(transactionService, never()).processTransactionWithPin(
                anyString(), anyString(), anyString(), anyDouble(), anyString(), anyString());
    }

    @Test
    void testProcessTransaction_MissingSenderAccountNumber() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderPin", "123456");
        requestBody.put("receiverAccountNumber", "9876543210");
        requestBody.put("amount", 100.0);
        requestBody.put("transactionMode", "UPI");

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Sender account number is required", apiResponse.getMessage());
    }

    @Test
    void testProcessTransaction_MissingReceiverAccountNumber() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "1234567890");
        requestBody.put("senderPin", "123456");
        requestBody.put("amount", 100.0);
        requestBody.put("transactionMode", "UPI");

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Receiver account number is required", apiResponse.getMessage());
    }

    @Test
    void testProcessTransaction_MissingAmount() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "1234567890");
        requestBody.put("senderPin", "123456");
        requestBody.put("receiverAccountNumber", "9876543210");
        requestBody.put("transactionMode", "UPI");

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Amount is required", apiResponse.getMessage());
    }

    @Test
    void testProcessTransaction_MissingTransactionMode() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "1234567890");
        requestBody.put("senderPin", "123456");
        requestBody.put("receiverAccountNumber", "9876543210");
        requestBody.put("amount", 100.0);

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Transaction mode is required", apiResponse.getMessage());
    }

    @Test
    void testProcessTransaction_InvalidAmountFormat() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "1234567890");
        requestBody.put("senderPin", "123456");
        requestBody.put("receiverAccountNumber", "9876543210");
        requestBody.put("amount", "invalid");
        requestBody.put("transactionMode", "UPI");

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Invalid amount format", apiResponse.getMessage());
    }

    @Test
    void testProcessTransaction_InvalidPin_TooShort() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "1234567890");
        requestBody.put("senderPin", "12"); // Invalid - too short
        requestBody.put("receiverAccountNumber", "9876543210");
        requestBody.put("amount", 100.0);
        requestBody.put("transactionMode", "UPI");

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid PIN format"));
    }

    @Test
    void testProcessTransaction_InvalidReceiverAccountNumber() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "1234567890");
        requestBody.put("senderPin", "123456");
        requestBody.put("receiverAccountNumber", "123"); // Invalid - too short
        requestBody.put("amount", 100.0);
        requestBody.put("transactionMode", "UPI");

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid receiver account number"));
    }

    @Test
    void testProcessTransaction_InvalidSenderAccountNumber() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "123"); // Invalid - too short
        requestBody.put("senderPin", "123456");
        requestBody.put("receiverAccountNumber", "9876543210");
        requestBody.put("amount", 100.0);
        requestBody.put("transactionMode", "UPI");

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid sender account number"));
    }

    @Test
    void testProcessTransaction_InvalidAmount_Zero() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "1234567890");
        requestBody.put("senderPin", "123456");
        requestBody.put("receiverAccountNumber", "9876543210");
        requestBody.put("amount", 0.0);
        requestBody.put("transactionMode", "UPI");

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid amount"));
    }

    @Test
    void testProcessTransaction_InvalidAmount_Negative() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "1234567890");
        requestBody.put("senderPin", "123456");
        requestBody.put("receiverAccountNumber", "9876543210");
        requestBody.put("amount", -100.0);
        requestBody.put("transactionMode", "UPI");

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid amount"));
    }

    @Test
    void testProcessTransaction_InvalidTransactionMode() {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "1234567890");
        requestBody.put("senderPin", "123456");
        requestBody.put("receiverAccountNumber", "9876543210");
        requestBody.put("amount", 100.0);
        requestBody.put("transactionMode", "INVALID_MODE");

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid transaction mode"));
    }

//    @Test
//    void testProcessTransaction_ServiceThrowsException() throws Exception {
//        // Arrange
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("senderAccountNumber", "1234567890");
//        requestBody.put("senderPin", "123456");
//        requestBody.put("receiverAccountNumber", "9876543210");
//        requestBody.put("amount", 100.0);
//        requestBody.put("transactionMode", "UPI");
//
//        when(transactionService.processTransactionWithPin(
//                anyString(), anyString(), anyString(), anyDouble(), anyString(), anyString()))
//                .thenThrow(new Exception("Database connection failed"));
//
//        // Act
//        Response response = transactionController.processTransaction(requestBody);
//
//        // Assert
//        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
//        ApiResponse apiResponse = (ApiResponse) response.getEntity();
//        assertFalse(apiResponse.isSuccess());
//        assertTrue(apiResponse.getMessage().contains("Transaction failed"));
//    }

    @Test
    void testProcessTransaction_WithoutDescription() throws Exception {
        // Arrange
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("senderAccountNumber", "1234567890");
        requestBody.put("senderPin", "123456");
        requestBody.put("receiverAccountNumber", "9876543210");
        requestBody.put("amount", 100.0);
        requestBody.put("transactionMode", "UPI");
        // No description

        Transaction mockTransaction = createMockTransaction();
        when(transactionService.processTransactionWithPin(
                anyString(), anyString(), anyString(), anyDouble(), anyString(), isNull()))
                .thenReturn(mockTransaction);

        // Act
        Response response = transactionController.processTransaction(requestBody);

        // Assert
        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        verify(transactionService, times(1)).processTransactionWithPin(
                eq("1234567890"),
                eq("123456"),
                eq("9876543210"),
                eq(100.0),
                eq("UPI"),
                isNull()
        );
    }

    // ==================== GET TRANSACTIONS BY ACCOUNT NUMBER TESTS ====================

    @Test
    void testGetTransactionsByAccountNumber_Success() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        List<Transaction> mockTransactions = Arrays.asList(
                createMockTransaction(),
                createMockTransaction()
        );

        when(transactionService.getTransactionsByAccountNumber(accountNumber))
                .thenReturn(mockTransactions);

        // Act
        Response response = transactionController.getTransactionsByAccountNumber(accountNumber, "json");

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertEquals("Transactions retrieved successfully", apiResponse.getMessage());
        assertNotNull(apiResponse.getData());

        verify(transactionService, times(1)).getTransactionsByAccountNumber(accountNumber);
    }

    @Test
    void testGetTransactionsByAccountNumber_InvalidAccountNumber() throws Exception {
        // Arrange
        String invalidAccountNumber = "123"; // Too short

        // Act
        Response response = transactionController.getTransactionsByAccountNumber(invalidAccountNumber, "json");

        // Assert
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid account number"));

        verify(transactionService, never()).getTransactionsByAccountNumber(anyString());
    }

    @Test
    void testGetTransactionsByAccountNumber_NoTransactionsFound() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        when(transactionService.getTransactionsByAccountNumber(accountNumber))
                .thenReturn(new ArrayList<>());

        // Act
        Response response = transactionController.getTransactionsByAccountNumber(accountNumber, "json");

        // Assert
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("No transactions found for this account", apiResponse.getMessage());

        verify(transactionService, times(1)).getTransactionsByAccountNumber(accountNumber);
    }

    @Test
    void testGetTransactionsByAccountNumber_CSVExport() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        List<Transaction> mockTransactions = Arrays.asList(createMockTransaction());

        when(transactionService.getTransactionsByAccountNumber(accountNumber))
                .thenReturn(mockTransactions);

        // Act
        Response response = transactionController.getTransactionsByAccountNumber(accountNumber, "csv");

        // Assert
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("text/csv", response.getMediaType().toString());
        assertNotNull(response.getEntity());

        verify(transactionService, times(1)).getTransactionsByAccountNumber(accountNumber);
    }
}