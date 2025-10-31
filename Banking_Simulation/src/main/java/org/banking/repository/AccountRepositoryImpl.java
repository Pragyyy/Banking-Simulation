package org.banking.repository;

import org.banking.config.DBConfig;
import org.banking.model.Account;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AccountRepositoryImpl implements AccountRepository {

    @Override
    public Account save(Account account) throws SQLException {
//        String sql = "INSERT INTO Account (account_id, customer_id, created_at, modified_at, " +
//                "balance, account_type, account_name, account_number, phone_number_linked, status) " +
//                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String sql = "INSERT INTO Account (account_id, customer_id, created_at, modified_at, " +
                "balance, account_type, account_name, account_number, phone_number_linked, ifsc_code, bank_name, status) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, account.getAccountId());
            ps.setString(2, account.getCustomerId());
            ps.setTimestamp(3, Timestamp.valueOf(account.getCreatedAt()));
            ps.setTimestamp(4, Timestamp.valueOf(account.getModifiedAt()));
            ps.setDouble(5, account.getBalance());
            ps.setString(6, account.getAccountType());
            ps.setString(7, account.getAccountName());
            ps.setString(8, account.getAccountNumber());
            ps.setString(9, account.getPhoneNumberLinked());
            ps.setString(10, account.getIfscCode());
            ps.setString(11, account.getBankName());
            ps.setString(12, account.getStatus());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                return account;
            }
            throw new SQLException("Failed to insert account");
        }
    }

    @Override
    public Account findById(String accountId) throws SQLException {
        String sql = "SELECT * FROM Account WHERE account_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
            return null;
        }
    }

    @Override
    public Account findByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT * FROM Account WHERE account_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
            return null;
        }
    }

    public Account findByAccountNumber(String accountNumber,Connection conn) throws SQLException {
        String sql = "SELECT * FROM Account WHERE account_number = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToAccount(rs);
            }
            return null;
        }
    }

    @Override
    public List<Account> findByCustomerId(String customerId) throws SQLException {
        String sql = "SELECT * FROM Account WHERE customer_id = ? ORDER BY created_at DESC";
        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        }
        return accounts;
    }

    @Override
    public Account update(String accountNumber, Account account) throws SQLException {
        String sql = "UPDATE Account SET account_type = ?, account_name = ?, " +
                "phone_number_linked = ?, status = ?, modified_at = ? WHERE account_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, account.getAccountType());
            ps.setString(2, account.getAccountName());
            ps.setString(3, account.getPhoneNumberLinked());
            ps.setString(4, account.getStatus());
            ps.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(6, accountNumber);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                return findByAccountNumber(accountNumber);
            }
            return null;
        }
    }

    @Override
    public boolean deleteByAccountNumber(String accountNumber) throws SQLException {
        String sql = "DELETE FROM Account WHERE account_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Account> findAll() throws SQLException {
        String sql = "SELECT * FROM Account ORDER BY created_at DESC";
        List<Account> accounts = new ArrayList<>();

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                accounts.add(mapResultSetToAccount(rs));
            }
        }
        return accounts;
    }

    @Override
    public boolean existsByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Account WHERE account_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    @Override
    public String getCustomerIdByAadhar(String aadharNumber) throws SQLException {
        String sql = "SELECT customer_id FROM Customer WHERE aadhar_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, aadharNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("customer_id");
            }
            return null;
        }
    }

    @Override
    public int getNextId() throws SQLException {
        return DBConfig.getNextId("Account", "account_id", "ACC_");
    }

    private Account mapResultSetToAccount(ResultSet rs) throws SQLException {
        Account account = new Account();
        account.setAccountId(rs.getString("account_id"));
        account.setCustomerId(rs.getString("customer_id"));

        Timestamp createdTimestamp = rs.getTimestamp("created_at");
        if (createdTimestamp != null) {
            account.setCreatedAt(createdTimestamp.toLocalDateTime());
        }

        Timestamp modifiedTimestamp = rs.getTimestamp("modified_at");
        if (modifiedTimestamp != null) {
            account.setModifiedAt(modifiedTimestamp.toLocalDateTime());
        }

        account.setBalance(rs.getDouble("balance"));
        account.setAccountType(rs.getString("account_type"));
        account.setAccountName(rs.getString("account_name"));
        account.setAccountNumber(rs.getString("account_number"));
        account.setPhoneNumberLinked(rs.getString("phone_number_linked"));
        account.setIfscCode(rs.getString("ifsc_code"));
        account.setBankName(rs.getString("bank_name"));
        account.setStatus(rs.getString("status"));

        return account;
    }
}