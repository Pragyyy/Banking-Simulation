package org.banking.service;

import org.banking.model.Account;
import org.banking.model.Customer;
import org.banking.model.Transaction;
import org.banking.repository.AccountRepository;
import org.banking.repository.AccountRepositoryImpl;
import org.banking.repository.CustomerRepository;
import org.banking.repository.CustomerRepositoryImpl;
import org.banking.repository.TransactionRepository;
import org.banking.repository.TransactionRepositoryImpl;

import java.util.List;

public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final NotificationService notificationService;

    // Default constructor for production use
    public TransactionServiceImpl() {
        this.transactionRepository = new TransactionRepositoryImpl();
        this.accountRepository = new AccountRepositoryImpl();
        this.customerRepository = new CustomerRepositoryImpl();
        this.notificationService = new NotificationService();
    }

    // Constructor for testing (allows dependency injection)
    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  AccountRepository accountRepository,
                                  CustomerRepository customerRepository,
                                  NotificationService notificationService) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.notificationService = notificationService;
    }

    @Override
    public Transaction processTransactionWithPin(String senderAccountNumber, String senderPin,
                                                 String receiverAccountNumber, Double amount,
                                                 String transactionMode, String description) throws Exception {

        // Step 1: Verify PIN
        boolean isPinValid = customerRepository.verifyPin(senderAccountNumber, senderPin);
        if (!isPinValid) {
            throw new Exception("Invalid PIN. Transaction denied.");
        }

        // Step 2: Get sender customer details
        Customer senderCustomer = customerRepository.findByAccountNumber(senderAccountNumber);
        if (senderCustomer == null) {
            throw new Exception("Sender customer not found with Account Number: " + senderAccountNumber);
        }

        // Step 3: Get sender's accounts
        List<Account> senderAccounts = accountRepository.findByCustomerId(senderCustomer.getCustomerId());
        if (senderAccounts.isEmpty()) {
            throw new Exception("No accounts found for sender");
        }

        // Use the first active account (you can modify this logic as needed)
        Account senderAccount = senderAccounts.stream()
                .filter(acc -> "Active".equalsIgnoreCase(acc.getStatus()))
                .findFirst()
                .orElseThrow(() -> new Exception("No active account found for sender"));


        // Step 4: Validate accounts are different
        if (senderAccountNumber.equals(receiverAccountNumber)) {
            throw new Exception("Sender and receiver account numbers must be different");
        }

        //Account senderAccount = accountRepository.findByAccountNumber(senderAccountNumber);
        if (senderAccount == null) {
            throw new Exception("Sender account not found: " + senderAccountNumber);
        }

        // Step 5: Get receiver account
        Account receiverAccount = accountRepository.findByAccountNumber(receiverAccountNumber);
        if (receiverAccount == null) {
            throw new Exception("Receiver account not found: " + receiverAccountNumber);
        }

        // Step 6: Get receiver customer details
        Customer receiverCustomer = customerRepository.findById(receiverAccount.getCustomerId());
        if (receiverCustomer == null) {
            throw new Exception("Receiver customer not found");
        }

        // Step 7: Validate both accounts are active
        if (!"Active".equalsIgnoreCase(senderAccount.getStatus())) {
            throw new Exception("Sender account is not active");
        }

        if (!"Active".equalsIgnoreCase(receiverAccount.getStatus())) {
            throw new Exception("Receiver account is not active");
        }

        // Step 8: Check insufficient balance
        if (senderAccount.getBalance() < amount) {
            throw new Exception("Insufficient balance. Available balance: " + senderAccount.getBalance());
        }

        // Step 9: Validate amount
        if (amount <= 0) {
            throw new Exception("Transaction amount must be greater than zero");
        }


        // Step 11: Process transaction in database
        Transaction transaction;
        try {
            transaction = transactionRepository.processMoneyTransfer(
                    senderAccountNumber,
                    receiverAccountNumber,
                    senderAccount,
                    receiverAccount,
                    amount,
                    transactionMode,
                    description
            );
        } catch (Exception e) {
            throw new Exception("Transaction failed: " + e.getMessage(), e);
        }

        // Step 12: Send email notifications (async - don't fail transaction if email fails)
        try {
            // Send email to sender
            notificationService.sendEmailAlertToSender(
                    senderCustomer.getEmail(),
                    senderCustomer.getName(),
                    receiverCustomer.getName(),
                    receiverAccountNumber,
                    amount,
                    transaction.getTransactionId(),
                    transaction.getTransactionTime(),
                    transactionMode,
                    description
            );

            // Send email to receiver
            notificationService.sendEmailAlertToReceiver(
                    receiverCustomer.getEmail(),
                    receiverCustomer.getName(),
                    senderCustomer.getName(),
                    senderAccountNumber,
                    amount,
                    transaction.getTransactionId(),
                    transaction.getTransactionTime(),
                    transactionMode,
                    description
            );

            System.out.println("Email notifications sent successfully");
        } catch (Exception emailException) {
            System.err.println("Failed to send email notifications: " + emailException.getMessage());
            // Don't fail the transaction if email fails
        }

        return transaction;
    }

    @Override
    public Transaction processTransaction(String senderAccountNumber, String receiverAccountNumber,
                                          Double amount, String transactionMode, String description) throws Exception {

        // Validation 1: Account numbers must be different
        if (senderAccountNumber.equals(receiverAccountNumber)) {
            throw new Exception("Sender and receiver account numbers must be different");
        }

        // Validation 2: Both accounts must exist
        Account senderAccount = accountRepository.findByAccountNumber(senderAccountNumber);
        if (senderAccount == null) {
            throw new Exception("Sender account not found: " + senderAccountNumber);
        }

        Account receiverAccount = accountRepository.findByAccountNumber(receiverAccountNumber);
        if (receiverAccount == null) {
            throw new Exception("Receiver account not found: " + receiverAccountNumber);
        }

        // Validation 3: Check if both accounts are active
        if (!"Active".equalsIgnoreCase(senderAccount.getStatus())) {
            throw new Exception("Sender account is not active");
        }

        if (!"Active".equalsIgnoreCase(receiverAccount.getStatus())) {
            throw new Exception("Receiver account is not active");
        }

        // Validation 4: Insufficient balance check
        if (senderAccount.getBalance() < amount) {
            throw new Exception("Insufficient balance. Available balance: " + senderAccount.getBalance());
        }

        // Validation 5: Amount must be positive
        if (amount <= 0) {
            throw new Exception("Transaction amount must be greater than zero");
        }

        try {
            // Delegate transaction processing to repository layer
            return transactionRepository.processMoneyTransfer(
                    senderAccountNumber,
                    receiverAccountNumber,
                    senderAccount,
                    receiverAccount,
                    amount,
                    transactionMode,
                    description
            );
        } catch (Exception e) {
            throw new Exception("Transaction failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Transaction getTransactionById(String transactionId) throws Exception {
        try {
            return transactionRepository.findById(transactionId);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve transaction: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> getTransactionsByAccountNumber(String accountNumber) throws Exception {
        try {
            // Verify account exists
            Account account = accountRepository.findByAccountNumber(accountNumber);
            if (account == null) {
                throw new Exception("Account not found: " + accountNumber);
            }

            return transactionRepository.findByAccountNumber(accountNumber);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve transactions: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Transaction> getAllTransactions() throws Exception {
        try {
            return transactionRepository.findAll();
        } catch (Exception e) {
            throw new Exception("Failed to retrieve transactions: " + e.getMessage(), e);
        }
    }
}