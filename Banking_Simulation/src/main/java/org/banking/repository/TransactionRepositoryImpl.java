package org.banking.repository;

import org.banking.config.DBConfig;
import org.banking.model.Account;
import org.banking.model.Transaction;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionRepositoryImpl implements TransactionRepository {

    @Override
    public Transaction save(Transaction transaction) throws SQLException {
        String sql = "INSERT INTO Transactions (transaction_id, account_id, transaction_amount, " +
                "transaction_type, transaction_time, transaction_mode, receiver_account_number, sender_account_number,description) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, transaction.getTransactionId());
            ps.setString(2, transaction.getAccountId());
            ps.setDouble(3, transaction.getTransactionAmount());
            ps.setString(4, transaction.getTransactionType());
            ps.setTimestamp(5, Timestamp.valueOf(transaction.getTransactionTime()));
            ps.setString(6, transaction.getTransactionMode());
            ps.setString(7, transaction.getReceiverAccountNumber());
            ps.setString(8, transaction.getSenderAccountNumber());
            ps.setString(9,transaction.getDescription());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                return transaction;
            }
            throw new SQLException("Failed to insert transaction");
        }
    }

    @Override
    public Transaction findById(String transactionId) throws SQLException {
        String sql = "SELECT * FROM Transactions WHERE transaction_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, transactionId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToTransaction(rs);
            }
            return null;
        }
    }

    @Override
    public List<Transaction> findByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT t.* FROM Transactions t " +
                "JOIN Account a ON t.account_id = a.account_id " +
                "WHERE a.account_number = ? " +
                "ORDER BY t.transaction_time DESC";

        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    @Override
    public List<Transaction> findBySenderAccountNumber(String senderAccountNumber) throws SQLException {
        String sql = "SELECT t.* FROM Transactions t " +
                "JOIN Account a ON t.account_id = a.account_id " +
                "WHERE a.account_number = ? AND t.transaction_type = 'DEBITED' " +
                "ORDER BY t.transaction_time DESC";

        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, senderAccountNumber);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    @Override
    public List<Transaction> findByReceiverAccountNumber(String receiverAccountNumber) throws SQLException {
        String sql = "SELECT t.* FROM Transactions t " +
                "WHERE t.receiver_details LIKE ? AND t.transaction_type = 'CREDITED' " +
                "ORDER BY t.transaction_time DESC";

        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + receiverAccountNumber + "%");
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    @Override
    public List<Transaction> findAll() throws SQLException {
        String sql = "SELECT * FROM Transactions ORDER BY transaction_time DESC";
        List<Transaction> transactions = new ArrayList<>();

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                transactions.add(mapResultSetToTransaction(rs));
            }
        }
        return transactions;
    }

    @Override
    public int getNextId() throws SQLException {
        return DBConfig.getNextId("Transactions", "transaction_id", "TXN_");
    }

    @Override
    public Transaction processMoneyTransfer(String senderAccountNumber, String receiverAccountNumber,
                                            Account senderAccount, Account receiverAccount,
                                            Double amount, String transactionMode, String description) throws SQLException {

        Connection conn = null;
        PreparedStatement psUpdateSender = null;
        PreparedStatement psUpdateReceiver = null;
        PreparedStatement psInsertDebit = null;
        PreparedStatement psInsertCredit = null;

        int baseId = getNextId(); // Only one DB call
        String debitId = String.format("TXN_%06d", baseId);
        String creditId = String.format("TXN_%06d", baseId + 1);

        try {
            // Get new connection
            conn = DBConfig.getConnection();

            if (conn == null || conn.isClosed()) {
                throw new SQLException("Unable to get database connection");
            }

            // Begin transaction
            conn.setAutoCommit(false);

            // Step 1: Update sender balance
            String updateSenderSql = "UPDATE Account SET balance = ?, modified_at = ? WHERE account_number = ?";
            psUpdateSender = conn.prepareStatement(updateSenderSql);
            psUpdateSender.setDouble(1, senderAccount.getBalance() - amount);
            psUpdateSender.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            psUpdateSender.setString(3, senderAccountNumber);
            if (psUpdateSender.executeUpdate() == 0)
                throw new SQLException("Failed to update sender account balance");

            // Step 2: Update receiver balance
            String updateReceiverSql = "UPDATE Account SET balance = ?, modified_at = ? WHERE account_number = ?";
            psUpdateReceiver = conn.prepareStatement(updateReceiverSql);
            psUpdateReceiver.setDouble(1, receiverAccount.getBalance() + amount);
            psUpdateReceiver.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            psUpdateReceiver.setString(3, receiverAccountNumber);
            if (psUpdateReceiver.executeUpdate() == 0)
                throw new SQLException("Failed to update receiver account balance");

            // Step 3: Generate unique transaction IDs safely
//            int baseId = getNextId(); // Only one DB call
//            String debitId = String.format("TXN_%06d", baseId);
//            String creditId = String.format("TXN_%06d", baseId + 1);

            LocalDateTime now = LocalDateTime.now();

            // Step 4: Insert DEBIT transaction
            String insertSql = "INSERT INTO Transactions (transaction_id, account_id, transaction_amount, " +
                    "transaction_type, transaction_time, transaction_mode, receiver_account_number, sender_account_number,description) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?,?)";

            psInsertDebit = conn.prepareStatement(insertSql);
            psInsertDebit.setString(1, debitId);
            psInsertDebit.setString(2, senderAccount.getAccountId());
            psInsertDebit.setDouble(3, amount);
            psInsertDebit.setString(4, "DEBITED");
            psInsertDebit.setTimestamp(5, Timestamp.valueOf(now));
            psInsertDebit.setString(6, transactionMode);
            psInsertDebit.setString(7, receiverAccountNumber);
            psInsertDebit.setString(8, senderAccountNumber);
            psInsertDebit.setString(9,description);
            if (psInsertDebit.executeUpdate() == 0)
                throw new SQLException("Failed to create debit transaction");

            // Step 5: Insert CREDIT transaction
            psInsertCredit = conn.prepareStatement(insertSql);
            psInsertCredit.setString(1, creditId);
            psInsertCredit.setString(2, receiverAccount.getAccountId());
            psInsertCredit.setDouble(3, amount);
            psInsertCredit.setString(4, "CREDITED");
            psInsertCredit.setTimestamp(5, Timestamp.valueOf(now));
            psInsertCredit.setString(6, transactionMode);
            psInsertCredit.setString(7, receiverAccountNumber);
            psInsertCredit.setString(8, senderAccountNumber);
            psInsertCredit.setString(9,description);
            if (psInsertCredit.executeUpdate() == 0)
                throw new SQLException("Failed to create credit transaction");

            // Commit only if everything succeeded
            conn.commit();

            System.out.println("Transaction success: Debit=" + debitId + " | Credit=" + creditId);

            // Build and return debit transaction
            Transaction debitTxn = new Transaction();
            debitTxn.setTransactionId(debitId);
            debitTxn.setAccountId(senderAccount.getAccountId());
            debitTxn.setTransactionAmount(amount);
            debitTxn.setTransactionType("DEBITED");
            debitTxn.setTransactionTime(now);
            debitTxn.setTransactionMode(transactionMode);
            debitTxn.setReceiverAccountNumber(receiverAccountNumber);
            debitTxn.setSenderAccountNumber(senderAccountNumber);
            debitTxn.setDescription(description);

            return debitTxn;
        }
        catch (Exception e) {
            // Rollback only if connection is still open
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.rollback();
                    System.err.println("Transaction rolled back due to error: " + e.getMessage());
                }
            } catch (SQLException rbEx) {
                throw new SQLException("Transaction failed and rollback failed: " + rbEx.getMessage());
            }

            throw new SQLException("Transaction failed: " + e.getMessage(), e);
        }
        finally {
            // Clean up properly
            try { if (psUpdateSender != null) psUpdateSender.close(); } catch (SQLException ignored) {}
            try { if (psUpdateReceiver != null) psUpdateReceiver.close(); } catch (SQLException ignored) {}
            try { if (psInsertDebit != null) psInsertDebit.close(); } catch (SQLException ignored) {}
            try { if (psInsertCredit != null) psInsertCredit.close(); } catch (SQLException ignored) {}

            try {
                if (conn != null && !conn.isClosed()) {
                    conn.setAutoCommit(true);
                    conn.close(); // Explicitly close connection after transaction
                }
            } catch (SQLException ignored) {}
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Transaction transaction = new Transaction();
        transaction.setTransactionId(rs.getString("transaction_id"));
        transaction.setAccountId(rs.getString("account_id"));
        transaction.setTransactionAmount(rs.getDouble("transaction_amount"));
        transaction.setTransactionType(rs.getString("transaction_type"));

        Timestamp timestamp = rs.getTimestamp("transaction_time");
        if (timestamp != null) {
            transaction.setTransactionTime(timestamp.toLocalDateTime());
        }

        transaction.setTransactionMode(rs.getString("transaction_mode"));
        transaction.setReceiverAccountNumber(rs.getString("receiver_account_number"));
        transaction.setSenderAccountNumber(rs.getString("sender_account_number"));
        transaction.setDescription(rs.getString("description"));

        return transaction;
    }
}