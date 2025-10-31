package org.banking.repository;

import org.banking.config.DBConfig;
import org.banking.model.Customer;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepositoryImpl implements CustomerRepository {

    @Override
    public Customer save(Customer customer) throws SQLException {
        String sql = "INSERT INTO Customer (customer_id, name, phone_number, email, address, " +
                "customer_pin, aadhar_number, dob, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getCustomerId());
            ps.setString(2, customer.getName());
            ps.setString(3, customer.getPhoneNumber());
            ps.setString(4, customer.getEmail());
            ps.setString(5, customer.getAddress());
            ps.setString(6, customer.getCustomerPin());
            ps.setString(7, customer.getAadharNumber());
            ps.setDate(8, new java.sql.Date(customer.getDob().getTime()));
            ps.setString(9, customer.getStatus());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                return customer;
            }
            throw new SQLException("Failed to insert customer");
        }
    }

    @Override
    public Customer findById(String customerId) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE customer_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customerId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
            return null;
        }
    }

    @Override
    public Customer findByAadhar(String aadharNumber) throws SQLException {
        String sql = "SELECT * FROM Customer WHERE aadhar_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, aadharNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
            return null;
        }
    }

    @Override
    public Customer findByAccountNumber(String accountNumber) throws SQLException {
        String sql = "SELECT c.* FROM Customer c JOIN Account a ON c.customer_id = a.customer_id WHERE a.account_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapResultSetToCustomer(rs);
            }
            return null;
        }
    }

    @Override
    public boolean verifyPin(String accountNumber, String pin) throws SQLException {
        String sql = "SELECT customer_pin FROM Customer c JOIN Account a ON c.customer_id = a.customer_id WHERE a.account_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, accountNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedPin = rs.getString("customer_pin");
                return storedPin != null && storedPin.equals(pin);
            }
            return false;
        }
    }

    @Override
    public Customer update(String customerId, Customer customer) throws SQLException {
        String sql = "UPDATE Customer SET name = ?, phone_number = ?, email = ?, address = ?, " +
                "customer_pin = ?, aadhar_number = ?, dob = ?, status = ? WHERE customer_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customer.getName());
            ps.setString(2, customer.getPhoneNumber());
            ps.setString(3, customer.getEmail());
            ps.setString(4, customer.getAddress());
            ps.setString(5, customer.getCustomerPin());
            ps.setString(6, customer.getAadharNumber());
            ps.setDate(7, new java.sql.Date(customer.getDob().getTime()));
            ps.setString(8, customer.getStatus());
            ps.setString(9, customerId);

            int rows = ps.executeUpdate();
            if (rows > 0) {
                customer.setCustomerId(customerId);
                return customer;
            }
            return null;
        }
    }

    @Override
    public boolean deleteById(String customerId) throws SQLException {
        String sql = "DELETE FROM Customer WHERE customer_id = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, customerId);
            return ps.executeUpdate() > 0;
        }
    }

    @Override
    public List<Customer> findAll() throws SQLException {
        String sql = "SELECT * FROM Customer ORDER BY customer_id";
        List<Customer> customers = new ArrayList<>();

        try (Connection conn = DBConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                customers.add(mapResultSetToCustomer(rs));
            }
        }
        return customers;
    }

    @Override
    public boolean existsByPhoneNumber(String phoneNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Customer WHERE phone_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phoneNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    @Override
    public boolean existsByEmail(String email) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Customer WHERE email = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    @Override
    public boolean existsByAadharNumber(String aadharNumber) throws SQLException {
        String sql = "SELECT COUNT(*) FROM Customer WHERE aadhar_number = ?";

        try (Connection conn = DBConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, aadharNumber);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    @Override
    public int getNextId() throws SQLException {
        return DBConfig.getNextId("Customer", "customer_id", "CUST_");
    }

    private Customer mapResultSetToCustomer(ResultSet rs) throws SQLException {
        Customer customer = new Customer();
        customer.setCustomerId(rs.getString("customer_id"));
        customer.setName(rs.getString("name"));
        customer.setPhoneNumber(rs.getString("phone_number"));
        customer.setEmail(rs.getString("email"));
        customer.setAddress(rs.getString("address"));
        // Don't set PIN when mapping - it will be hidden by @JsonProperty annotation
        customer.setCustomerPin(rs.getString("customer_pin"));
        customer.setAadharNumber(rs.getString("aadhar_number"));
        customer.setDob(rs.getDate("dob"));
        customer.setStatus(rs.getString("status"));
        return customer;
    }
}