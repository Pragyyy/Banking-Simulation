package org.banking.service;

import org.banking.model.Transaction;
import java.util.List;

public interface TransactionService {
    Transaction processTransaction(String senderAccountNumber, String receiverAccountNumber,
                                   Double amount, String transactionMode, String description) throws Exception;

    // New method with PIN verification and email notifications
    Transaction processTransactionWithPin(String senderAccountNumber, String senderPin,
                                          String receiverAccountNumber, Double amount,
                                          String transactionMode, String description) throws Exception;

    Transaction getTransactionById(String transactionId) throws Exception;
    List<Transaction> getTransactionsByAccountNumber(String accountNumber) throws Exception;
    List<Transaction> getAllTransactions() throws Exception;
}