package org.banking.repository;

import org.banking.model.Account;
import org.banking.model.Transaction;
import java.sql.SQLException;
import java.util.List;

public interface TransactionRepository {
    Transaction save(Transaction transaction) throws SQLException;
    Transaction findById(String transactionId) throws SQLException;
    List<Transaction> findByAccountNumber(String accountNumber) throws SQLException;
    List<Transaction> findBySenderAccountNumber(String senderAccountNumber) throws SQLException;
    List<Transaction> findByReceiverAccountNumber(String receiverAccountNumber) throws SQLException;
    List<Transaction> findAll() throws SQLException;
    int getNextId() throws SQLException;

    // Process complete money transfer with database transaction
    Transaction processMoneyTransfer(String senderAccountNumber, String receiverAccountNumber,
                                     Account senderAccount, Account receiverAccount,
                                     Double amount, String transactionMode, String description
                                     ) throws SQLException;
}