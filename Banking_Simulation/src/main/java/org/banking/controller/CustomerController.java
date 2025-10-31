package org.banking.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.banking.model.ApiResponse;
import org.banking.model.Customer;
import org.banking.service.CustomerService;
import org.banking.service.CustomerServiceImpl;
import org.banking.util.ValidationUtil;
import java.util.List;

@Path("/customer")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerController {

    private CustomerService customerService;

    // Default constructor for production use
    public CustomerController() {
        this.customerService = new CustomerServiceImpl();
    }

    // Constructor for testing (allows dependency injection)
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @POST
    @Path("/onboard")
    public Response createCustomer(Customer customer) {
        try {
            // Validation
            if (!ValidationUtil.isValidName(customer.getName())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid name. Name must contain only alphabets and spaces"))
                        .build();
            }

            if (!ValidationUtil.isValidPhoneNumber(customer.getPhoneNumber())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid phone number. Must be 10 digits and cannot start with 0"))
                        .build();
            }

            if (!ValidationUtil.isValidEmail(customer.getEmail())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid email format"))
                        .build();
            }

            if (!ValidationUtil.isValidAadhar(customer.getAadharNumber())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid Aadhar number. Must be 12 digits"))
                        .build();
            }

            if (!ValidationUtil.isValidPin(customer.getCustomerPin())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid PIN. Must be 4-6 digits"))
                        .build();
            }

            if (!ValidationUtil.isValidDOB(customer.getDob())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid date of birth. Customer must be at least 18 years old"))
                        .build();
            }

            // Check for duplicates
            if (customerService.isPhoneNumberExists(customer.getPhoneNumber())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(ApiResponse.error("Phone number already exists"))
                        .build();
            }

            if (customerService.isEmailExists(customer.getEmail())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(ApiResponse.error("Email already exists"))
                        .build();
            }

            if (customerService.isAadharExists(customer.getAadharNumber())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(ApiResponse.error("Aadhar number already exists"))
                        .build();
            }

            Customer createdCustomer = customerService.createCustomer(customer);
            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Customer created successfully", createdCustomer))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to create customer: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/{customer_id}")
    public Response getCustomer(@PathParam("customer_id") String customerId) {
        try {
            Customer customer = customerService.getCustomerById(customerId);
            if (customer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("Customer not found"))
                        .build();
            }
            return Response.ok(ApiResponse.success("Customer retrieved successfully", customer))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to retrieve customer: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("aadhar/{aadhar_number}")
    public Response getCustomerByAadhar(@PathParam("aadhar_number") String aadhar) {
        try {
            Customer customer = customerService.getCustomerByAadhar(aadhar);
            if (customer == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("Customer not found"))
                        .build();
            }
            return Response.ok(ApiResponse.success("Customer retrieved successfully", customer))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to retrieve customer: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{customer_id}")
    public Response updateCustomer(@PathParam("customer_id") String customerId, Customer customer) {
        try {
            // Check if customer exists
            Customer existing = customerService.getCustomerById(customerId);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("Customer not found"))
                        .build();
            }

            // Validation for updated fields
            if (!ValidationUtil.isValidName(customer.getName())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid name"))
                        .build();
            }

            if (!ValidationUtil.isValidPhoneNumber(customer.getPhoneNumber())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid phone number"))
                        .build();
            }

            if (!ValidationUtil.isValidEmail(customer.getEmail())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid email"))
                        .build();
            }

            Customer updatedCustomer = customerService.updateCustomer(customerId, customer);
            return Response.ok(ApiResponse.success("Customer updated successfully", updatedCustomer))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to update customer: " + e.getMessage()))
                    .build();
        }
    }

//    @POST
//    @Path("/login")
//    public Response login(Customer loginRequest) {
//        try {
//            Customer customer = customerService.getCustomerByAadhar(loginRequest.getAadharNumber());
//            if (customer == null) {
//                return Response.status(Response.Status.UNAUTHORIZED)
//                        .entity(ApiResponse.error("Invalid Aadhar number or not registered"))
//                        .build();
//            }
//
//            if (!customer.getCustomerPin().equals(loginRequest.getCustomerPin())) {
//                return Response.status(Response.Status.UNAUTHORIZED)
//                        .entity(ApiResponse.error("Invalid PIN"))
//                        .build();
//            }
//
//            return Response.ok(ApiResponse.success("Login successful", customer)).build();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                    .entity(ApiResponse.error("Login failed: " + e.getMessage()))
//                    .build();
//        }
//    }


    @DELETE
    @Path("/{customer_id}")
    public Response deleteCustomer(@PathParam("customer_id") String customerId) {
        try {
            boolean deleted = customerService.deleteCustomer(customerId);
            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("Customer not found"))
                        .build();
            }
            return Response.ok(ApiResponse.success("Customer deleted successfully"))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to delete customer: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllCustomers() {
        try {
            List<Customer> customers = customerService.getAllCustomers();
            return Response.ok(ApiResponse.success("Customers retrieved successfully", customers))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to retrieve customers: " + e.getMessage()))
                    .build();
        }
    }
}