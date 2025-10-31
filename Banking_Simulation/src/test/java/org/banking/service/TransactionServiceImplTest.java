package org.banking.service;

import org.banking.model.Account;
import org.banking.model.Customer;
import org.banking.model.Transaction;
import org.banking.repository.AccountRepository;
import org.banking.repository.CustomerRepository;
import org.banking.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private NotificationService notificationService;

    private TransactionServiceImpl transactionService;

    @BeforeEach
    void setUp() {
        transactionService = new TransactionServiceImpl(
                transactionRepository,
                accountRepository,
                customerRepository,
                notificationService
        );
    }

    // Helper methods to create mock objects
    private Customer createMockSenderCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId("CUST_000001");
        customer.setName("John Doe");
        customer.setEmail("john@example.com");
        customer.setPhoneNumber("9876543210");
        customer.setAadharNumber("123456789012");
        customer.setCustomerPin("123456");
        customer.setStatus("Active");
        return customer;
    }

    private Customer createMockReceiverCustomer() {
        Customer customer = new Customer();
        customer.setCustomerId("CUST_000002");
        customer.setName("Jane Smith");
        customer.setEmail("jane@example.com");
        customer.setPhoneNumber("9123456789");
        customer.setAadharNumber("234567890123");
        customer.setStatus("Active");
        return customer;
    }

    private Account createMockSenderAccount() {
        Account account = new Account();
        account.setAccountId("ACC_000001");
        account.setCustomerId("CUST_000001");
        account.setAccountNumber("1234567890");
        account.setAccountName("John Doe");
        account.setBalance(1000.0);
        account.setStatus("Active");
        account.setAccountType("SAVINGS");
        account.setCreatedAt(LocalDateTime.now());
        account.setModifiedAt(LocalDateTime.now());
        return account;
    }

    private Account createMockReceiverAccount() {
        Account account = new Account();
        account.setAccountId("ACC_000002");
        account.setCustomerId("CUST_000002");
        account.setAccountNumber("9876543210");
        account.setAccountName("Jane Smith");
        account.setBalance(500.0);
        account.setStatus("Active");
        account.setAccountType("SAVINGS");
        account.setCreatedAt(LocalDateTime.now());
        account.setModifiedAt(LocalDateTime.now());
        return account;
    }

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
    void testProcessTransactionWithPin_Success() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String senderPin = "123456";
        String receiverAccountNumber = "9876543210";
        Double amount = 100.0;
        String transactionMode = "UPI";
        String description = "Test payment";

        Customer senderCustomer = createMockSenderCustomer();
        Customer receiverCustomer = createMockReceiverCustomer();
        Account senderAccount = createMockSenderAccount();
        Account receiverAccount = createMockReceiverAccount();
        Transaction mockTransaction = createMockTransaction();

        when(customerRepository.verifyPin(senderAccountNumber, senderPin)).thenReturn(true);
        when(customerRepository.findByAccountNumber(senderAccountNumber)).thenReturn(senderCustomer);
        when(accountRepository.findByCustomerId(senderCustomer.getCustomerId()))
                .thenReturn(Arrays.asList(senderAccount));
        when(accountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(receiverAccount);
        when(customerRepository.findById(receiverAccount.getCustomerId())).thenReturn(receiverCustomer);
        when(transactionRepository.processMoneyTransfer(
                anyString(), anyString(), any(Account.class), any(Account.class),
                anyDouble(), anyString(), anyString()))
                .thenReturn(mockTransaction);

        // Act
        Transaction result = transactionService.processTransactionWithPin(
                senderAccountNumber, senderPin, receiverAccountNumber, amount, transactionMode, description
        );

        // Assert
        assertNotNull(result);
        assertEquals("TXN_000001", result.getTransactionId());
        assertEquals(100.0, result.getTransactionAmount());

        verify(customerRepository, times(1)).verifyPin(senderAccountNumber, senderPin);
        verify(transactionRepository, times(1)).processMoneyTransfer(
                eq(senderAccountNumber), eq(receiverAccountNumber), eq(senderAccount),
                eq(receiverAccount), eq(amount), eq(transactionMode), eq(description)
        );
        verify(notificationService, times(1)).sendEmailAlertToSender(
                eq(senderCustomer.getEmail()), anyString(), anyString(), anyString(),
                eq(amount), anyString(), any(LocalDateTime.class), eq(transactionMode), eq(description)
        );
        verify(notificationService, times(1)).sendEmailAlertToReceiver(
                eq(receiverCustomer.getEmail()), anyString(), anyString(), anyString(),
                eq(amount), anyString(), any(LocalDateTime.class), eq(transactionMode), eq(description)
        );
    }

    @Test
    void testProcessTransactionWithPin_InvalidPin() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String senderPin = "999999"; // Wrong PIN
        String receiverAccountNumber = "9876543210";

        when(customerRepository.verifyPin(senderAccountNumber, senderPin)).thenReturn(false);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransactionWithPin(
                        senderAccountNumber, senderPin, receiverAccountNumber, 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Invalid PIN"));
        verify(customerRepository, times(1)).verifyPin(senderAccountNumber, senderPin);
        verify(transactionRepository, never()).processMoneyTransfer(
                anyString(), anyString(), any(), any(), anyDouble(), anyString(), anyString()
        );
    }

    @Test
    void testProcessTransactionWithPin_SenderCustomerNotFound() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String senderPin = "123456";

        when(customerRepository.verifyPin(senderAccountNumber, senderPin)).thenReturn(true);
        when(customerRepository.findByAccountNumber(senderAccountNumber)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransactionWithPin(
                        senderAccountNumber, senderPin, "9876543210", 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Sender customer not found"));
        verify(customerRepository, times(1)).findByAccountNumber(senderAccountNumber);
    }

    @Test
    void testProcessTransactionWithPin_NoAccountsForSender() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String senderPin = "123456";
        Customer senderCustomer = createMockSenderCustomer();

        when(customerRepository.verifyPin(senderAccountNumber, senderPin)).thenReturn(true);
        when(customerRepository.findByAccountNumber(senderAccountNumber)).thenReturn(senderCustomer);
        when(accountRepository.findByCustomerId(senderCustomer.getCustomerId()))
                .thenReturn(new ArrayList<>());

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransactionWithPin(
                        senderAccountNumber, senderPin, "9876543210", 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("No accounts found for sender"));
    }

    @Test
    void testProcessTransactionWithPin_NoActiveAccountForSender() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String senderPin = "123456";
        Customer senderCustomer = createMockSenderCustomer();
        Account inactiveAccount = createMockSenderAccount();
        inactiveAccount.setStatus("Inactive");

        when(customerRepository.verifyPin(senderAccountNumber, senderPin)).thenReturn(true);
        when(customerRepository.findByAccountNumber(senderAccountNumber)).thenReturn(senderCustomer);
        when(accountRepository.findByCustomerId(senderCustomer.getCustomerId()))
                .thenReturn(Arrays.asList(inactiveAccount));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransactionWithPin(
                        senderAccountNumber, senderPin, "9876543210", 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("No active account found for sender"));
    }

    @Test
    void testProcessTransactionWithPin_SameAccountNumbers() throws Exception {
        // Arrange
        String accountNumber = "1234567890";
        String senderPin = "123456";
        Customer senderCustomer = createMockSenderCustomer();
        Account senderAccount = createMockSenderAccount();

        when(customerRepository.verifyPin(accountNumber, senderPin)).thenReturn(true);
        when(customerRepository.findByAccountNumber(accountNumber)).thenReturn(senderCustomer);
        when(accountRepository.findByCustomerId(senderCustomer.getCustomerId()))
                .thenReturn(Arrays.asList(senderAccount));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransactionWithPin(
                        accountNumber, senderPin, accountNumber, 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Sender and receiver account numbers must be different"));
    }

    @Test
    void testProcessTransactionWithPin_ReceiverAccountNotFound() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String senderPin = "123456";
        String receiverAccountNumber = "9876543210";
        Customer senderCustomer = createMockSenderCustomer();
        Account senderAccount = createMockSenderAccount();

        when(customerRepository.verifyPin(senderAccountNumber, senderPin)).thenReturn(true);
        when(customerRepository.findByAccountNumber(senderAccountNumber)).thenReturn(senderCustomer);
        when(accountRepository.findByCustomerId(senderCustomer.getCustomerId()))
                .thenReturn(Arrays.asList(senderAccount));
        when(accountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransactionWithPin(
                        senderAccountNumber, senderPin, receiverAccountNumber, 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Receiver account not found"));
    }

    @Test
    void testProcessTransactionWithPin_ReceiverCustomerNotFound() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String senderPin = "123456";
        String receiverAccountNumber = "9876543210";
        Customer senderCustomer = createMockSenderCustomer();
        Account senderAccount = createMockSenderAccount();
        Account receiverAccount = createMockReceiverAccount();

        when(customerRepository.verifyPin(senderAccountNumber, senderPin)).thenReturn(true);
        when(customerRepository.findByAccountNumber(senderAccountNumber)).thenReturn(senderCustomer);
        when(accountRepository.findByCustomerId(senderCustomer.getCustomerId()))
                .thenReturn(Arrays.asList(senderAccount));
        when(accountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(receiverAccount);
        when(customerRepository.findById(receiverAccount.getCustomerId())).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransactionWithPin(
                        senderAccountNumber, senderPin, receiverAccountNumber, 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Receiver customer not found"));
    }

    @Test
    void testProcessTransactionWithPin_ReceiverAccountNotActive() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String senderPin = "123456";
        String receiverAccountNumber = "9876543210";
        Customer senderCustomer = createMockSenderCustomer();
        Account senderAccount = createMockSenderAccount();
        Account receiverAccount = createMockReceiverAccount();
        receiverAccount.setStatus("Blocked");
        Customer receiverCustomer = createMockReceiverCustomer();

        when(customerRepository.verifyPin(senderAccountNumber, senderPin)).thenReturn(true);
        when(customerRepository.findByAccountNumber(senderAccountNumber)).thenReturn(senderCustomer);
        when(accountRepository.findByCustomerId(senderCustomer.getCustomerId()))
                .thenReturn(Arrays.asList(senderAccount));
        when(accountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(receiverAccount);
        when(customerRepository.findById(receiverAccount.getCustomerId())).thenReturn(receiverCustomer);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransactionWithPin(
                        senderAccountNumber, senderPin, receiverAccountNumber, 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Receiver account is not active"));
    }

    @Test
    void testProcessTransactionWithPin_InsufficientBalance() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String senderPin = "123456";
        String receiverAccountNumber = "9876543210";
        Customer senderCustomer = createMockSenderCustomer();
        Account senderAccount = createMockSenderAccount();
        senderAccount.setBalance(50.0); // Less than transaction amount
        Account receiverAccount = createMockReceiverAccount();
        Customer receiverCustomer = createMockReceiverCustomer();

        when(customerRepository.verifyPin(senderAccountNumber, senderPin)).thenReturn(true);
        when(customerRepository.findByAccountNumber(senderAccountNumber)).thenReturn(senderCustomer);
        when(accountRepository.findByCustomerId(senderCustomer.getCustomerId()))
                .thenReturn(Arrays.asList(senderAccount));
        when(accountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(receiverAccount);
        when(customerRepository.findById(receiverAccount.getCustomerId())).thenReturn(receiverCustomer);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransactionWithPin(
                        senderAccountNumber, senderPin, receiverAccountNumber, 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Insufficient balance"));
    }

    @Test
    void testProcessTransactionWithPin_ZeroAmount() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String senderPin = "123456";
        String receiverAccountNumber = "9876543210";
        Customer senderCustomer = createMockSenderCustomer();
        Account senderAccount = createMockSenderAccount();
        Account receiverAccount = createMockReceiverAccount();
        Customer receiverCustomer = createMockReceiverCustomer();

        when(customerRepository.verifyPin(senderAccountNumber, senderPin)).thenReturn(true);
        when(customerRepository.findByAccountNumber(senderAccountNumber)).thenReturn(senderCustomer);
        when(accountRepository.findByCustomerId(senderCustomer.getCustomerId()))
                .thenReturn(Arrays.asList(senderAccount));
        when(accountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(receiverAccount);
        when(customerRepository.findById(receiverAccount.getCustomerId())).thenReturn(receiverCustomer);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransactionWithPin(
                        senderAccountNumber, senderPin, receiverAccountNumber, 0.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Transaction amount must be greater than zero"));
    }

    @Test
    void testProcessTransactionWithPin_TransactionRepositoryThrowsException() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String senderPin = "123456";
        String receiverAccountNumber = "9876543210";
        Double amount = 100.0;
        Customer senderCustomer = createMockSenderCustomer();
        Customer receiverCustomer = createMockReceiverCustomer();
        Account senderAccount = createMockSenderAccount();
        Account receiverAccount = createMockReceiverAccount();

        when(customerRepository.verifyPin(senderAccountNumber, senderPin)).thenReturn(true);
        when(customerRepository.findByAccountNumber(senderAccountNumber)).thenReturn(senderCustomer);
        when(accountRepository.findByCustomerId(senderCustomer.getCustomerId()))
                .thenReturn(Arrays.asList(senderAccount));
        when(accountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(receiverAccount);
        when(customerRepository.findById(receiverAccount.getCustomerId())).thenReturn(receiverCustomer);
        when(transactionRepository.processMoneyTransfer(
                anyString(), anyString(), any(), any(), anyDouble(), anyString(), anyString()))
                .thenThrow(new SQLException("Database error"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransactionWithPin(
                        senderAccountNumber, senderPin, receiverAccountNumber, amount, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Transaction failed"));
    }

    @Test
    void testProcessTransactionWithPin_EmailNotificationFails() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String senderPin = "123456";
        String receiverAccountNumber = "9876543210";
        Double amount = 100.0;
        Customer senderCustomer = createMockSenderCustomer();
        Customer receiverCustomer = createMockReceiverCustomer();
        Account senderAccount = createMockSenderAccount();
        Account receiverAccount = createMockReceiverAccount();
        Transaction mockTransaction = createMockTransaction();

        when(customerRepository.verifyPin(senderAccountNumber, senderPin)).thenReturn(true);
        when(customerRepository.findByAccountNumber(senderAccountNumber)).thenReturn(senderCustomer);
        when(accountRepository.findByCustomerId(senderCustomer.getCustomerId()))
                .thenReturn(Arrays.asList(senderAccount));
        when(accountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(receiverAccount);
        when(customerRepository.findById(receiverAccount.getCustomerId())).thenReturn(receiverCustomer);
        when(transactionRepository.processMoneyTransfer(
                anyString(), anyString(), any(), any(), anyDouble(), anyString(), anyString()))
                .thenReturn(mockTransaction);

        doThrow(new RuntimeException("Email service down"))
                .when(notificationService).sendEmailAlertToSender(
                        anyString(), anyString(), anyString(), anyString(),
                        anyDouble(), anyString(), any(), anyString(), anyString()
                );

        // Act - should not throw exception even if email fails
        Transaction result = transactionService.processTransactionWithPin(
                senderAccountNumber, senderPin, receiverAccountNumber, amount, "UPI", "Test"
        );

        // Assert
        assertNotNull(result);
        assertEquals("TXN_000001", result.getTransactionId());
    }

    // ==================== PROCESS TRANSACTION (WITHOUT PIN) TESTS ====================

    @Test
    void testProcessTransaction_Success() throws Exception {
        // Arrange
        String senderAccountNumber = "1234567890";
        String receiverAccountNumber = "9876543210";
        Double amount = 100.0;
        Account senderAccount = createMockSenderAccount();
        Account receiverAccount = createMockReceiverAccount();
        Transaction mockTransaction = createMockTransaction();

        when(accountRepository.findByAccountNumber(senderAccountNumber)).thenReturn(senderAccount);
        when(accountRepository.findByAccountNumber(receiverAccountNumber)).thenReturn(receiverAccount);
        when(transactionRepository.processMoneyTransfer(
                anyString(), anyString(), any(), any(), anyDouble(), anyString(), anyString()))
                .thenReturn(mockTransaction);

        // Act
        Transaction result = transactionService.processTransaction(
                senderAccountNumber, receiverAccountNumber, amount, "NEFT", "Test"
        );

        // Assert
        assertNotNull(result);
        assertEquals("TXN_000001", result.getTransactionId());
        verify(transactionRepository, times(1)).processMoneyTransfer(
                eq(senderAccountNumber), eq(receiverAccountNumber), eq(senderAccount),
                eq(receiverAccount), eq(amount), eq("NEFT"), eq("Test")
        );
    }

    @Test
    void testProcessTransaction_SameAccountNumbers() throws Exception {
        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransaction(
                        "1234567890", "1234567890", 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Sender and receiver account numbers must be different"));
    }

    @Test
    void testProcessTransaction_SenderAccountNotFound() throws Exception {
        // Arrange
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransaction(
                        "1234567890", "9876543210", 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Sender account not found"));
    }

    @Test
    void testProcessTransaction_ReceiverAccountNotFound() throws Exception {
        // Arrange
        Account senderAccount = createMockSenderAccount();
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(senderAccount);
        when(accountRepository.findByAccountNumber("9876543210")).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransaction(
                        "1234567890", "9876543210", 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Receiver account not found"));
    }

    @Test
    void testProcessTransaction_InsufficientBalance() throws Exception {
        // Arrange
        Account senderAccount = createMockSenderAccount();
        senderAccount.setBalance(50.0);
        Account receiverAccount = createMockReceiverAccount();

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(senderAccount);
        when(accountRepository.findByAccountNumber("9876543210")).thenReturn(receiverAccount);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.processTransaction(
                        "1234567890", "9876543210", 100.0, "UPI", "Test"
                )
        );

        assertTrue(exception.getMessage().contains("Insufficient balance"));
    }

    // ==================== GET TRANSACTION BY ID TESTS ====================

    @Test
    void testGetTransactionById_Success() throws Exception {
        // Arrange
        Transaction mockTransaction = createMockTransaction();
        when(transactionRepository.findById("TXN_000001")).thenReturn(mockTransaction);

        // Act
        Transaction result = transactionService.getTransactionById("TXN_000001");

        // Assert
        assertNotNull(result);
        assertEquals("TXN_000001", result.getTransactionId());
        verify(transactionRepository, times(1)).findById("TXN_000001");
    }

    @Test
    void testGetTransactionById_NotFound() throws Exception {
        // Arrange
        when(transactionRepository.findById("TXN_999999")).thenReturn(null);

        // Act
        Transaction result = transactionService.getTransactionById("TXN_999999");

        // Assert
        assertNull(result);
        verify(transactionRepository, times(1)).findById("TXN_999999");
    }

    @Test
    void testGetTransactionById_RepositoryThrowsException() throws Exception {
        // Arrange
        when(transactionRepository.findById("TXN_000001"))
                .thenThrow(new SQLException("Database error"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.getTransactionById("TXN_000001")
        );

        assertTrue(exception.getMessage().contains("Failed to retrieve transaction"));
    }

    // ==================== GET TRANSACTIONS BY ACCOUNT NUMBER TESTS ====================

    @Test
    void testGetTransactionsByAccountNumber_Success() throws Exception {
        // Arrange
        Account account = createMockSenderAccount();
        List<Transaction> mockTransactions = Arrays.asList(
                createMockTransaction(),
                createMockTransaction()
        );

        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(account);
        when(transactionRepository.findByAccountNumber("1234567890")).thenReturn(mockTransactions);

        // Act
        List<Transaction> result = transactionService.getTransactionsByAccountNumber("1234567890");

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(accountRepository, times(1)).findByAccountNumber("1234567890");
        verify(transactionRepository, times(1)).findByAccountNumber("1234567890");
    }

    @Test
    void testGetTransactionsByAccountNumber_AccountNotFound() throws Exception {
        // Arrange
        when(accountRepository.findByAccountNumber("9999999999")).thenReturn(null);

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.getTransactionsByAccountNumber("9999999999")
        );

        assertTrue(exception.getMessage().contains("Account not found"));
        verify(transactionRepository, never()).findByAccountNumber(anyString());
    }

    @Test
    void testGetTransactionsByAccountNumber_EmptyList() throws Exception {
        // Arrange
        Account account = createMockSenderAccount();
        when(accountRepository.findByAccountNumber("1234567890")).thenReturn(account);
        when(transactionRepository.findByAccountNumber("1234567890"))
                .thenReturn(new ArrayList<>());

        // Act
        List<Transaction> result = transactionService.getTransactionsByAccountNumber("1234567890");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== GET ALL TRANSACTIONS TESTS ====================

    @Test
    void testGetAllTransactions_Success() throws Exception {
        // Arrange
        List<Transaction> mockTransactions = Arrays.asList(
                createMockTransaction(),
                createMockTransaction(),
                createMockTransaction()
        );
        when(transactionRepository.findAll()).thenReturn(mockTransactions);

        // Act
        List<Transaction> result = transactionService.getAllTransactions();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void testGetAllTransactions_EmptyList() throws Exception {
        // Arrange
        when(transactionRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Transaction> result = transactionService.getAllTransactions();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(transactionRepository, times(1)).findAll();
    }

    @Test
    void testGetAllTransactions_RepositoryThrowsException() throws Exception {
        // Arrange
        when(transactionRepository.findAll())
                .thenThrow(new SQLException("Database error"));

        // Act & Assert
        Exception exception = assertThrows(Exception.class, () ->
                transactionService.getAllTransactions()
        );

        assertTrue(exception.getMessage().contains("Failed to retrieve transactions"));
    }
}