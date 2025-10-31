package org.banking.service;

import org.banking.model.Customer;

import java.util.List;

public interface CustomerService {
    Customer createCustomer(Customer customer) throws Exception;
    Customer getCustomerById(String customerId) throws Exception;
    Customer getCustomerByAadhar(String aadhar) throws Exception;
    Customer updateCustomer(String customerId, Customer customer) throws Exception;
    boolean deleteCustomer(String customerId) throws Exception;
    List<Customer> getAllCustomers() throws Exception;

    boolean isPhoneNumberExists(String phoneNumber) throws Exception;

    boolean isEmailExists(String email) throws Exception;

    boolean isAadharExists(String aadharNumber) throws Exception;


//    boolean validatePhoneNumber(String phoneNumber);
//    String generateCustomerId() throws Exception;
}