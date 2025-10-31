package org.banking.service;

import org.banking.model.Customer;
import org.banking.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;

    private CustomerServiceImpl customerService;

    private Customer validCustomer;
    private SimpleDateFormat dateFormat;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Inject the mock repository through constructor
        customerService = new CustomerServiceImpl(customerRepository);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        validCustomer = new Customer();
        validCustomer.setName("Jane Smith");
        validCustomer.setPhoneNumber("9123456789");
        validCustomer.setEmail("jane.smith@example.com");
        validCustomer.setAddress("456 Park Avenue");
        validCustomer.setCustomerPin("654321");
        validCustomer.setAadharNumber("987654321098");
        validCustomer.setDob(dateFormat.parse("1985-05-15"));
    }

    // ========== CREATE CUSTOMER TESTS ==========

    @Test
    void testCreateCustomer_Success() throws Exception {
        when(customerRepository.getNextId()).thenReturn(1);
        when(customerRepository.save(any(Customer.class))).thenReturn(validCustomer);

        Customer result = customerService.createCustomer(validCustomer);

        assertNotNull(result);
        assertEquals("CUST_000001", result.getCustomerId());
        assertEquals("Inactive", result.getStatus());
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testCreateCustomer_GeneratesCorrectId() throws Exception {
        when(customerRepository.getNextId()).thenReturn(42);
        when(customerRepository.save(any(Customer.class))).thenReturn(validCustomer);

        Customer result = customerService.createCustomer(validCustomer);

        assertEquals("CUST_000042", result.getCustomerId());
    }

    @Test
    void testCreateCustomer_PreservesExistingStatus() throws Exception {
        validCustomer.setStatus("Active");
        when(customerRepository.getNextId()).thenReturn(1);
        when(customerRepository.save(any(Customer.class))).thenReturn(validCustomer);

        Customer result = customerService.createCustomer(validCustomer);

        assertEquals("Active", result.getStatus());
    }

    @Test
    void testCreateCustomer_RepositoryException() throws Exception {
        when(customerRepository.getNextId()).thenReturn(1);
        when(customerRepository.save(any(Customer.class)))
                .thenThrow(new SQLException("Database connection failed"));

        Exception exception = assertThrows(Exception.class, () -> {
            customerService.createCustomer(validCustomer);
        });

        assertTrue(exception.getMessage().contains("Failed to create customer"));
    }

    // ========== GET CUSTOMER BY ID TESTS ==========

    @Test
    void testGetCustomerById_Success() throws Exception {
        validCustomer.setCustomerId("CUST_000001");
        when(customerRepository.findById("CUST_000001")).thenReturn(validCustomer);

        Customer result = customerService.getCustomerById("CUST_000001");

        assertNotNull(result);
        assertEquals("CUST_000001", result.getCustomerId());
        assertEquals("Jane Smith", result.getName());
        verify(customerRepository, times(1)).findById("CUST_000001");
    }

    @Test
    void testGetCustomerById_NotFound() throws Exception {
        when(customerRepository.findById("CUST_999999")).thenReturn(null);

        Customer result = customerService.getCustomerById("CUST_999999");

        assertNull(result);
    }

    @Test
    void testGetCustomerById_RepositoryException() throws Exception {
        when(customerRepository.findById(anyString()))
                .thenThrow(new SQLException("Database error"));

        Exception exception = assertThrows(Exception.class, () -> {
            customerService.getCustomerById("CUST_000001");
        });

        assertTrue(exception.getMessage().contains("Failed to retrieve customer"));
    }

    // ========== UPDATE CUSTOMER TESTS ==========

    @Test
    void testUpdateCustomer_Success() throws Exception {
        validCustomer.setCustomerId("CUST_000001");
        when(customerRepository.update("CUST_000001", validCustomer)).thenReturn(validCustomer);

        Customer result = customerService.updateCustomer("CUST_000001", validCustomer);

        assertNotNull(result);
        assertEquals("CUST_000001", result.getCustomerId());
        verify(customerRepository, times(1)).update("CUST_000001", validCustomer);
    }

    @Test
    void testUpdateCustomer_NotFound() throws Exception {
        when(customerRepository.update("CUST_999999", validCustomer)).thenReturn(null);

        Exception exception = assertThrows(Exception.class, () -> {
            customerService.updateCustomer("CUST_999999", validCustomer);
        });

        assertTrue(exception.getMessage().contains("Customer not found"));
    }

    @Test
    void testUpdateCustomer_RepositoryException() throws Exception {
        when(customerRepository.update(anyString(), any(Customer.class)))
                .thenThrow(new SQLException("Update failed"));

        Exception exception = assertThrows(Exception.class, () -> {
            customerService.updateCustomer("CUST_000001", validCustomer);
        });

        assertTrue(exception.getMessage().contains("Failed to update customer"));
    }

    // ========== DELETE CUSTOMER TESTS ==========

    @Test
    void testDeleteCustomer_Success() throws Exception {
        when(customerRepository.deleteById("CUST_000001")).thenReturn(true);

        boolean result = customerService.deleteCustomer("CUST_000001");

        assertTrue(result);
        verify(customerRepository, times(1)).deleteById("CUST_000001");
    }

    @Test
    void testDeleteCustomer_NotFound() throws Exception {
        when(customerRepository.deleteById("CUST_999999")).thenReturn(false);

        boolean result = customerService.deleteCustomer("CUST_999999");

        assertFalse(result);
    }

    @Test
    void testDeleteCustomer_RepositoryException() throws Exception {
        when(customerRepository.deleteById(anyString()))
                .thenThrow(new SQLException("Delete failed"));

        Exception exception = assertThrows(Exception.class, () -> {
            customerService.deleteCustomer("CUST_000001");
        });

        assertTrue(exception.getMessage().contains("Failed to delete customer"));
    }

    // ========== GET ALL CUSTOMERS TESTS ==========

    @Test
    void testGetAllCustomers_Success() throws Exception {
        Customer customer2 = new Customer();
        customer2.setCustomerId("CUST_000002");
        customer2.setName("Bob Johnson");

        List<Customer> customers = Arrays.asList(validCustomer, customer2);
        when(customerRepository.findAll()).thenReturn(customers);

        List<Customer> result = customerService.getAllCustomers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(customerRepository, times(1)).findAll();
    }

    @Test
    void testGetAllCustomers_EmptyList() throws Exception {
        when(customerRepository.findAll()).thenReturn(Arrays.asList());

        List<Customer> result = customerService.getAllCustomers();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== EXISTENCE CHECK TESTS ==========

    @Test
    void testIsPhoneNumberExists_True() throws Exception {
        when(customerRepository.existsByPhoneNumber("9123456789")).thenReturn(true);

        boolean result = customerService.isPhoneNumberExists("9123456789");

        assertTrue(result);
    }

    @Test
    void testIsPhoneNumberExists_False() throws Exception {
        when(customerRepository.existsByPhoneNumber("9999999999")).thenReturn(false);

        boolean result = customerService.isPhoneNumberExists("9999999999");

        assertFalse(result);
    }

    @Test
    void testIsEmailExists_True() throws Exception {
        when(customerRepository.existsByEmail("jane.smith@example.com")).thenReturn(true);

        boolean result = customerService.isEmailExists("jane.smith@example.com");

        assertTrue(result);
    }

    @Test
    void testIsEmailExists_False() throws Exception {
        when(customerRepository.existsByEmail("new@example.com")).thenReturn(false);

        boolean result = customerService.isEmailExists("new@example.com");

        assertFalse(result);
    }

    @Test
    void testIsAadharExists_True() throws Exception {
        when(customerRepository.existsByAadharNumber("987654321098")).thenReturn(true);

        boolean result = customerService.isAadharExists("987654321098");

        assertTrue(result);
    }

    @Test
    void testIsAadharExists_False() throws Exception {
        when(customerRepository.existsByAadharNumber("111111111111")).thenReturn(false);

        boolean result = customerService.isAadharExists("111111111111");

        assertFalse(result);
    }

    @Test
    void testIsPhoneNumberExists_Exception() throws Exception {
        when(customerRepository.existsByPhoneNumber(anyString()))
                .thenThrow(new SQLException("Database error"));

        Exception exception = assertThrows(Exception.class, () -> {
            customerService.isPhoneNumberExists("9123456789");
        });

        assertTrue(exception.getMessage().contains("Failed to check phone number"));
    }
}