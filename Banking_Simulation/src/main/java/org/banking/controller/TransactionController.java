package org.banking.controller;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;
import org.banking.model.ApiResponse;
import org.banking.model.Transaction;
import org.banking.service.TransactionService;
import org.banking.service.TransactionServiceImpl;
import org.banking.util.ValidationUtil;

import java.util.List;
import java.util.Map;

@Path("/transaction")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TransactionController {

    private TransactionService transactionService;

    // Default constructor for production use
    public TransactionController() {
        this.transactionService = new TransactionServiceImpl();
    }

    // Constructor for testing (allows dependency injection)
    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    /**
     * POST /api/transaction/process
     * Process a transaction between two accounts with PIN verification
     *
     * Request Body:
     * {
     *   "senderAccountNumber": "123456789012",
     *   "senderPin": "123456",
     *   "receiverAccountNumber": "0987654321",
     *   "amount": 100.00,
     *   "transactionMode": "UPI",
     *   "description": "Payment for services"
     * }
     */
    @POST
    @Path("/process")
    public Response processTransaction(Map<String, Object> requestBody) {
        try {
            // Extract and validate request parameters
            String senderAccountNumber = (String) requestBody.get("senderAccountNumber");
            String senderPin = (String) requestBody.get("senderPin");
            String receiverAccountNumber = (String) requestBody.get("receiverAccountNumber");
            Object amountObj = requestBody.get("amount");
            String transactionMode = (String) requestBody.get("transactionMode");
            String description = (String) requestBody.get("description");

            // Validation - Required fields

            if (senderPin == null || senderPin.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Sender PIN is required"))
                        .build();
            }

            if (senderAccountNumber == null || senderAccountNumber.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Sender account number is required"))
                        .build();
            }

            if (receiverAccountNumber == null || receiverAccountNumber.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Receiver account number is required"))
                        .build();
            }

            if (amountObj == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Amount is required"))
                        .build();
            }

            if (transactionMode == null || transactionMode.trim().isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Transaction mode is required"))
                        .build();
            }

            // Convert amount to Double
            Double amount;
            try {
                if (amountObj instanceof Number) {
                    amount = ((Number) amountObj).doubleValue();
                } else {
                    amount = Double.parseDouble(amountObj.toString());
                }
            } catch (NumberFormatException e) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid amount format"))
                        .build();
            }

            // Validation - PIN
            if (!ValidationUtil.isValidPin(senderPin)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid PIN format. Must be 4-6 digits"))
                        .build();
            }

            // Validation - Account number
            if (!ValidationUtil.isValidAccountNumber(receiverAccountNumber)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid receiver account number. Must be 10-18 digits"))
                        .build();
            }

            if (!ValidationUtil.isValidAccountNumber(senderAccountNumber)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid sender account number. Must be 10-18 digits"))
                        .build();
            }

            // Validation - Amount
            if (!ValidationUtil.isValidAmount(amount)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid amount. Must be greater than zero"))
                        .build();
            }

            // Validation - Transaction mode
            if (!ValidationUtil.isValidTransactionMode(transactionMode)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid transaction mode. Must be DEBIT, UPI, CREDIT CARD, CASH, TRANSFER, NEFT, IMPS, or RTGS"))
                        .build();
            }

            // Process transaction with PIN verification
            Transaction transaction = transactionService.processTransactionWithPin(
                    senderAccountNumber,
                    senderPin,
                    receiverAccountNumber,
                    amount,
                    transactionMode.toUpperCase(),
                    description
            );

            return Response.status(Response.Status.CREATED)
                    .entity(ApiResponse.success("Transaction processed successfully. Email notifications sent.", transaction))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Transaction failed: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/transaction/{account_number}
     * Get all transactions for an account number
     */

    @GET
    @Path("/{account_number}")
    public Response getTransactionsByAccountNumber(
            @PathParam("account_number") String accountNumber,
            @QueryParam("export") @DefaultValue("json") String exportType) {

        try {
            // Validate account number
            if (!ValidationUtil.isValidAccountNumber(accountNumber)) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(ApiResponse.error("Invalid account number. Must be 10-18 digits"))
                        .build();
            }

            List<Transaction> transactions = transactionService.getTransactionsByAccountNumber(accountNumber);

            if (transactions.isEmpty()) {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(ApiResponse.error("No transactions found for this account"))
                        .build();
            }

            // Check if CSV export is requested
            if ("csv".equalsIgnoreCase(exportType)) {
                StreamingOutput stream = output -> {
                    String header = "Transaction ID,SenderAccountNumber, ReceiverAccountNumber,Amount,Type,Description,Date & Time\n";
                    output.write(header.getBytes());

                    for (Transaction tx : transactions) {
                        String row = String.format("%s,%s,%s,%.2f,%s,%s,%s\n",
                                tx.getTransactionId(),
                                tx.getSenderAccountNumber(),
                                tx.getReceiverAccountNumber(),
                                tx.getTransactionAmount(),
                                tx.getTransactionType(),
                                tx.getDescription(),
                                tx.getTransactionTime());
                        output.write(row.getBytes());
                    }

                    output.flush();
                };

                return Response.ok(stream, "text/csv")
                        .header("Content-Disposition", "attachment; filename=transactions_" + accountNumber + ".csv")
                        .build();
            }

            // Default: return JSON
            return Response.ok(ApiResponse.success("Transactions retrieved successfully", transactions))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to retrieve transactions: " + e.getMessage()))
                    .build();
        }
    }

    /**
     * GET /api/transaction/all
     * Get all transactions
     */
    @GET
    @Path("/all")
    public Response getAllTransactions() {
        try {
            List<Transaction> transactions = transactionService.getAllTransactions();

            return Response.ok(ApiResponse.success("Transactions retrieved successfully", transactions))
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(ApiResponse.error("Failed to retrieve transactions: " + e.getMessage()))
                    .build();
        }
    }
}