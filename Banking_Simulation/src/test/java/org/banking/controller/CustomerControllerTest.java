package org.banking.controller;

import org.banking.model.ApiResponse;
import org.banking.model.Customer;
import org.banking.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.ws.rs.core.Response;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class CustomerControllerTest {

    @Mock
    private CustomerService customerService;

    private CustomerController customerController;

    private Customer validCustomer;
    private SimpleDateFormat dateFormat;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Inject the mock service through constructor
        customerController = new CustomerController(customerService);

        dateFormat = new SimpleDateFormat("yyyy-MM-dd");

        validCustomer = new Customer();
        validCustomer.setCustomerId("CUST_000001");
        validCustomer.setName("John Doe");
        validCustomer.setPhoneNumber("9876543210");
        validCustomer.setEmail("john.doe@example.com");
        validCustomer.setAddress("123 Main Street");
        validCustomer.setCustomerPin("123456");
        validCustomer.setAadharNumber("123456789012");
        validCustomer.setDob(dateFormat.parse("1990-01-01"));
        validCustomer.setStatus("Active");
    }

    // ========== CREATE CUSTOMER TESTS ==========

    @Test
    void testCreateCustomer_Success() throws Exception {
        when(customerService.isPhoneNumberExists(anyString())).thenReturn(false);
        when(customerService.isEmailExists(anyString())).thenReturn(false);
        when(customerService.isAadharExists(anyString())).thenReturn(false);
        when(customerService.createCustomer(any(Customer.class))).thenReturn(validCustomer);

        Response response = customerController.createCustomer(validCustomer);

        assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertEquals("Customer created successfully", apiResponse.getMessage());
        verify(customerService, times(1)).createCustomer(any(Customer.class));
    }

    @Test
    void testCreateCustomer_InvalidName() {
        Customer customer = new Customer();
        customer.setName("John123"); // Invalid - contains numbers
        customer.setPhoneNumber("9876543210");
        customer.setEmail("john@example.com");
        customer.setAadharNumber("123456789012");
        customer.setCustomerPin("1234");

        Response response = customerController.createCustomer(customer);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid name"));
    }

    @Test
    void testCreateCustomer_InvalidPhoneNumber_StartsWithZero() {
        validCustomer.setPhoneNumber("0987654321"); // Invalid - starts with 0

        Response response = customerController.createCustomer(validCustomer);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid phone number"));
    }

    @Test
    void testCreateCustomer_InvalidPhoneNumber_NotTenDigits() {
        validCustomer.setPhoneNumber("98765"); // Invalid - not 10 digits

        Response response = customerController.createCustomer(validCustomer);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
    }

    @Test
    void testCreateCustomer_InvalidPhoneNumber_ContainsAlpha() {
        validCustomer.setPhoneNumber("987abc4321"); // Invalid - contains letters

        Response response = customerController.createCustomer(validCustomer);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
    }

    @Test
    void testCreateCustomer_InvalidEmail() {
        validCustomer.setEmail("invalid-email"); // Invalid email format

        Response response = customerController.createCustomer(validCustomer);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid email"));
    }

    @Test
    void testCreateCustomer_InvalidAadhar_NotTwelveDigits() {
        validCustomer.setAadharNumber("12345"); // Invalid - not 12 digits

        Response response = customerController.createCustomer(validCustomer);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid Aadhar"));
    }

    @Test
    void testCreateCustomer_InvalidAadhar_ContainsAlpha() {
        validCustomer.setAadharNumber("12345678901a"); // Invalid - contains letter

        Response response = customerController.createCustomer(validCustomer);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
    }

    @Test
    void testCreateCustomer_InvalidPin() {
        validCustomer.setCustomerPin("12"); // Invalid - less than 4 digits

        Response response = customerController.createCustomer(validCustomer);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("Invalid PIN"));
    }

    @Test
    void testCreateCustomer_InvalidDOB_UnderAge() throws Exception {
        validCustomer.setDob(dateFormat.parse("2020-01-01")); // Under 18

        Response response = customerController.createCustomer(validCustomer);

        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertTrue(apiResponse.getMessage().contains("at least 18 years old"));
    }

    @Test
    void testCreateCustomer_DuplicatePhoneNumber() throws Exception {
        when(customerService.isPhoneNumberExists(anyString())).thenReturn(true);

        Response response = customerController.createCustomer(validCustomer);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Phone number already exists", apiResponse.getMessage());
    }

    @Test
    void testCreateCustomer_DuplicateEmail() throws Exception {
        when(customerService.isPhoneNumberExists(anyString())).thenReturn(false);
        when(customerService.isEmailExists(anyString())).thenReturn(true);

        Response response = customerController.createCustomer(validCustomer);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Email already exists", apiResponse.getMessage());
    }

    @Test
    void testCreateCustomer_DuplicateAadhar() throws Exception {
        when(customerService.isPhoneNumberExists(anyString())).thenReturn(false);
        when(customerService.isEmailExists(anyString())).thenReturn(false);
        when(customerService.isAadharExists(anyString())).thenReturn(true);

        Response response = customerController.createCustomer(validCustomer);

        assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Aadhar number already exists", apiResponse.getMessage());
    }

    // ========== GET CUSTOMER TESTS ==========

    @Test
    void testGetCustomer_Success() throws Exception {
        when(customerService.getCustomerById("CUST_000001")).thenReturn(validCustomer);

        Response response = customerController.getCustomer("CUST_000001");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertNotNull(apiResponse.getData());
    }

    @Test
    void testGetCustomer_NotFound() throws Exception {
        when(customerService.getCustomerById("CUST_999999")).thenReturn(null);

        Response response = customerController.getCustomer("CUST_999999");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
        assertEquals("Customer not found", apiResponse.getMessage());
    }

    // ========== UPDATE CUSTOMER TESTS ==========

    @Test
    void testUpdateCustomer_Success() throws Exception {
        when(customerService.getCustomerById("CUST_000001")).thenReturn(validCustomer);
        when(customerService.updateCustomer(anyString(), any(Customer.class)))
                .thenReturn(validCustomer);

        Response response = customerController.updateCustomer("CUST_000001", validCustomer);

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertEquals("Customer updated successfully", apiResponse.getMessage());
    }

    @Test
    void testUpdateCustomer_NotFound() throws Exception {
        when(customerService.getCustomerById("CUST_999999")).thenReturn(null);

        Response response = customerController.updateCustomer("CUST_999999", validCustomer);

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
    }

    // ========== DELETE CUSTOMER TESTS ==========

    @Test
    void testDeleteCustomer_Success() throws Exception {
        when(customerService.deleteCustomer("CUST_000001")).thenReturn(true);

        Response response = customerController.deleteCustomer("CUST_000001");

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertEquals("Customer deleted successfully", apiResponse.getMessage());
    }

    @Test
    void testDeleteCustomer_NotFound() throws Exception {
        when(customerService.deleteCustomer("CUST_999999")).thenReturn(false);

        Response response = customerController.deleteCustomer("CUST_999999");

        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertFalse(apiResponse.isSuccess());
    }

    // ========== GET ALL CUSTOMERS TESTS ==========

    @Test
    void testGetAllCustomers_Success() throws Exception {
        List<Customer> customers = Arrays.asList(validCustomer);
        when(customerService.getAllCustomers()).thenReturn(customers);

        Response response = customerController.getAllCustomers();

        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        ApiResponse apiResponse = (ApiResponse) response.getEntity();
        assertTrue(apiResponse.isSuccess());
        assertNotNull(apiResponse.getData());
    }
}