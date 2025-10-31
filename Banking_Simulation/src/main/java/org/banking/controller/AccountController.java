package org.banking.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.banking.model.Account;
import org.banking.model.ApiResponse;
import org.banking.service.AccountService;
import org.banking.service.AccountServiceImpl;
import org.banking.util.ValidationUtil;
import java.util.List;

@Path("/account")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AccountController {

    private final AccountService accountService;

    // Default constructor for production use
    public AccountController() {
        this.accountService = new AccountServiceImpl();
    }

    // Constructor for testing (allows dependency injection)
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @POST
    @Path("/create")
    public Response createAccount(Account account) {
        try {
            // Validation - aadharNumber is now required instead of customerId
            if (account.getAadharNumber() == null || account.getAadharNumber().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Aadhar number is required"))
                        .build();
            }

            if (!ValidationUtil.isValidAadhar(account.getAadharNumber())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid Aadhar number. Must be 12 digits"))
                        .build();
            }

            if (!ValidationUtil.isValidAccountNumber(account.getAccountNumber())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid account number. Must be 10-18 digits"))
                        .build();
            }

            if (!ValidationUtil.isValidPhoneNumber(account.getPhoneNumberLinked())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid phone number. Must be 10 digits and cannot start with 0"))
                        .build();
            }

            if (!ValidationUtil.isValidName(account.getAccountName())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid account name. Name must contain only alphabets and spaces"))
                        .build();
            }

            if (!ValidationUtil.isValidAccountType(account.getAccountType())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid account type. Must be SAVINGS, CURRENT, FIXED, or RECURRING"))
                        .build();
            }

            if (account.getIfscCode() == null || account.getIfscCode().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("IFSC code is required"))
                        .build();
            }

            if (account.getBankName() == null || account.getBankName().trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Bank name is required"))
                        .build();
            }

            // Check for duplicates
            if (accountService.isAccountNumberExists(account.getAccountNumber())) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(ApiResponse.error("Account number already exists"))
                        .build();
            }

            Account createdAccount = accountService.createAccount(account);
            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Account created successfully", createdAccount))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to create account: " + e.getMessage()))
                    .build();
        }
    }

//    @GET
//    @Path("/{account_id}")
//    public Response getAccountById(@PathParam("account_id") String accountId) {
//        try {
//            Account account = accountService.getAccountById(accountId);
//            if (account == null) {
//                return Response.status(Response.Status.NOT_FOUND)
//                        .entity(ApiResponse.error("Account not found"))
//                        .build();
//            }
//            return Response.ok(ApiResponse.success("Account retrieved successfully", account))
//                    .build();
//        } catch (Exception e) {
//            e.printStackTrace();
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
//                    .entity(ApiResponse.error("Failed to retrieve account: " + e.getMessage()))
//                    .build();
//        }
//    }

    @GET
    @Path("/{account_number}")
    public Response getAccountByNumber(@PathParam("account_number") String accountNumber) {
        try {
            Account account = accountService.getAccountByAccountNumber(accountNumber);
            if (account == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("Account not found"))
                        .build();
            }
            return Response.ok(ApiResponse.success("Account retrieved successfully", account))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to retrieve account: " + e.getMessage()))
                    .build();
        }
    }


    @GET
    @Path("/aadhar/{aadhar_number}")
    public Response getAccountsByAadhar(@PathParam("aadhar_number") String aadharNumber) {
        try {
            if (!ValidationUtil.isValidAadhar(aadharNumber)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid Aadhar number. Must be 12 digits"))
                        .build();
            }

            List<Account> accounts = accountService.getAccountsByAadhar(aadharNumber);
            if (accounts.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("No accounts found for this Aadhar number"))
                        .build();
            }
            return Response.ok(ApiResponse.success("Accounts retrieved successfully", accounts))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to retrieve accounts: " + e.getMessage()))
                    .build();
        }
    }

    @DELETE
    @Path("/{account_number}")
    public Response deleteAccount(@PathParam("account_number") String accountNumber) {
        try {
            boolean deleted = accountService.deleteAccount(accountNumber);
            if (!deleted) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("Account not found"))
                        .build();
            }
            return Response.ok(ApiResponse.success("Account deleted successfully"))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to delete account: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{account_number}")
    public Response updateAccount(@PathParam("account_number") String accountNumber, Account account) {
        try {
            // Check if account exists
            Account existing = accountService.getAccountByAccountNumber(accountNumber);
            if (existing == null) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("Account not found"))
                        .build();
            }

            // Validation for updated fields
            if (!ValidationUtil.isValidName(account.getAccountName())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid account name"))
                        .build();
            }

            if (!ValidationUtil.isValidPhoneNumber(account.getPhoneNumberLinked())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid phone number"))
                        .build();
            }

            if (!ValidationUtil.isValidAccountType(account.getAccountType())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid account type"))
                        .build();
            }

            if (account.getStatus() != null && !ValidationUtil.isValidStatus(account.getStatus())) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid status"))
                        .build();
            }

            Account updatedAccount = accountService.updateAccount(accountNumber, account);
            return Response.ok(ApiResponse.success("Account updated successfully", updatedAccount))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to update account: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllAccounts() {
        try {
            List<Account> accounts = accountService.getAllAccounts();
            return Response.ok(ApiResponse.success("Accounts retrieved successfully", accounts))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to retrieve accounts: " + e.getMessage()))
                    .build();
        }
    }
}