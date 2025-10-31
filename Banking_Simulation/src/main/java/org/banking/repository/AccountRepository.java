package org.banking.repository;

import org.banking.model.Account;
import java.sql.SQLException;
import java.util.List;

public interface AccountRepository {
    Account save(Account account) throws SQLException;
    Account findById(String accountId) throws SQLException;
    Account findByAccountNumber(String accountNumber) throws SQLException;
    List<Account> findByCustomerId(String customerId) throws SQLException;
    Account update(String accountNumber, Account account) throws SQLException;
    boolean deleteByAccountNumber(String accountNumber) throws SQLException;
    List<Account> findAll() throws SQLException;
    boolean existsByAccountNumber(String accountNumber) throws SQLException;
    String getCustomerIdByAadhar(String aadharNumber) throws SQLException;
    int getNextId() throws SQLException;
}