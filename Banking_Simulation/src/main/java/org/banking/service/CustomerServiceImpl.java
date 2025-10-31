package org.banking.service;

import org.banking.model.Customer;
import org.banking.repository.CustomerRepository;
import org.banking.repository.CustomerRepositoryImpl;
import java.util.List;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    // Default constructor for production use
    public CustomerServiceImpl() {
        this.customerRepository = new CustomerRepositoryImpl();
    }

    // Constructor for testing (allows dependency injection)
    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer createCustomer(Customer customer) throws Exception {
        try {
            // Generate customer ID
            int nextId = customerRepository.getNextId();
            String customerId = String.format("CUST_%06d", nextId);
            customer.setCustomerId(customerId);

            // Default status to Inactive if not provided
            if (customer.getStatus() == null || customer.getStatus().isEmpty()) {
                customer.setStatus("Inactive");
            }

            return customerRepository.save(customer);
        } catch (Exception e) {
            throw new Exception("Failed to create customer: " + e.getMessage(), e);
        }
    }

    @Override
    public Customer getCustomerById(String customerId) throws Exception {
        try {
            return customerRepository.findById(customerId);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve customer: " + e.getMessage(), e);
        }
    }

    @Override
    public Customer updateCustomer(String customerId, Customer customer) throws Exception {
        try {
            Customer updated = customerRepository.update(customerId, customer);
            if (updated == null) {
                throw new Exception("Customer not found with ID: " + customerId);
            }
            if (updated.getCustomerPin() == null || updated.getCustomerPin().isEmpty()) {
                updated.setCustomerPin(customer.getCustomerPin());
            }

            return updated;
        } catch (Exception e) {
            throw new Exception("Failed to update customer: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteCustomer(String customerId) throws Exception {
        try {
            return customerRepository.deleteById(customerId);
        } catch (Exception e) {
            throw new Exception("Failed to delete customer: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Customer> getAllCustomers() throws Exception {
        try {
            return customerRepository.findAll();
        } catch (Exception e) {
            throw new Exception("Failed to retrieve customers: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isPhoneNumberExists(String phoneNumber) throws Exception {
        try {
            return customerRepository.existsByPhoneNumber(phoneNumber);
        } catch (Exception e) {
            throw new Exception("Failed to check phone number: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isEmailExists(String email) throws Exception {
        try {
            return customerRepository.existsByEmail(email);
        } catch (Exception e) {
            throw new Exception("Failed to check email: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAadharExists(String aadharNumber) throws Exception {
        try {
            return customerRepository.existsByAadharNumber(aadharNumber);
        } catch (Exception e) {
            throw new Exception("Failed to check aadhar number: " + e.getMessage(), e);
        }
    }

    @Override
    public Customer getCustomerByAadhar(String aadharNumber) throws Exception {
        try {
            return customerRepository.findByAadhar(aadharNumber);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve customer: " + e.getMessage(), e);
        }
    }

}
