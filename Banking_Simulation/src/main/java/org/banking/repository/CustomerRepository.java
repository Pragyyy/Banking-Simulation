package org.banking.repository;

import org.banking.model.Customer;
import java.sql.SQLException;
import java.util.List;

public interface CustomerRepository {
    Customer save(Customer customer) throws SQLException;
    Customer findById(String customerId) throws SQLException;
    Customer update(String customerId, Customer customer) throws SQLException;
    boolean deleteById(String customerId) throws SQLException;
    List<Customer> findAll() throws SQLException;
    boolean existsByPhoneNumber(String phoneNumber) throws SQLException;
    boolean existsByEmail(String email) throws SQLException;
    boolean existsByAadharNumber(String aadharNumber) throws SQLException;
    int getNextId() throws SQLException;

    Customer findByAccountNumber(String accountNumber) throws SQLException;

    // New method for PIN verification
    boolean verifyPin(String accountNumber, String pin) throws SQLException;

    // New method to get customer by Aadhar
    Customer findByAadhar(String aadharNumber) throws SQLException;
}