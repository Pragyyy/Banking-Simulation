package org.banking.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class ValidationUtil {

    // Regex patterns
    private static final String PHONE_PATTERN = "^[1-9]\\d{9}$";
    private static final String EMAIL_PATTERN = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
    private static final String NAME_PATTERN = "^[a-zA-Z\\s]{2,100}$";
    private static final String AADHAR_PATTERN = "^\\d{12}$";
    private static final String PIN_PATTERN = "^\\d{4,6}$";
    private static final String ACCOUNT_NUMBER_PATTERN = "^[0-9]{10,18}$";

    // Compiled patterns for performance
    private static final Pattern phonePattern = Pattern.compile(PHONE_PATTERN);
    private static final Pattern emailPattern = Pattern.compile(EMAIL_PATTERN);
    private static final Pattern namePattern = Pattern.compile(NAME_PATTERN);
    private static final Pattern aadharPattern = Pattern.compile(AADHAR_PATTERN);
    private static final Pattern pinPattern = Pattern.compile(PIN_PATTERN);
    private static final Pattern accountNumberPattern = Pattern.compile(ACCOUNT_NUMBER_PATTERN);

    /**
     * Validates phone number - must be 10 digits and cannot start with 0
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return false;
        }
        return phonePattern.matcher(phoneNumber.trim()).matches();
    }

    /**
     * Validates email format
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return emailPattern.matcher(email.trim()).matches();
    }

    /**
     * Validates name - only alphabets and spaces allowed
     */
    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return namePattern.matcher(name.trim()).matches();
    }

    /**
     * Validates Aadhar number - must be 12 digits
     */
    public static boolean isValidAadhar(String aadhar) {
        if (aadhar == null || aadhar.trim().isEmpty()) {
            return false;
        }
        return aadharPattern.matcher(aadhar.trim()).matches();
    }

    /**
     * Validates PIN - must be 4-6 digits
     */
    public static boolean isValidPin(String pin) {
        if (pin == null || pin.trim().isEmpty()) {
            return false;
        }
        return pinPattern.matcher(pin.trim()).matches();
    }

    /**
     * Validates account number - must be 10-18 digits
     */
    public static boolean isValidAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            return false;
        }
        return accountNumberPattern.matcher(accountNumber.trim()).matches();
    }

    /**
     * Validates amount - must be positive
     */
    public static boolean isValidAmount(double amount) {
        return amount > 0;
    }

    /**
     * Validates IFSC code format
     * Format: First 4 characters are alphabets, 5th character is 0, last 6 characters are alphanumeric
     * Example: SBIN0001234
     */
    public static boolean isValidIfscCode(String ifscCode) {
        if (ifscCode == null || ifscCode.trim().isEmpty()) {
            return false;
        }
        String IFSC_PATTERN = "^[A-Z]{4}0[A-Z0-9]{6}$";
        Pattern ifscPattern = Pattern.compile(IFSC_PATTERN);
        return ifscPattern.matcher(ifscCode.trim().toUpperCase()).matches();
    }

    /**
     * Validates bank name - only alphabets and spaces allowed
     */
    public static boolean isValidBankName(String bankName) {
        if (bankName == null || bankName.trim().isEmpty()) {
            return false;
        }
        String BANK_NAME_PATTERN = "^[a-zA-Z\\s]{2,100}$";
        Pattern bankNamePattern = Pattern.compile(BANK_NAME_PATTERN);
        return bankNamePattern.matcher(bankName.trim()).matches();
    }

    /**
     * Validates transaction type
     */
    public static boolean isValidTransactionType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return false;
        }
        String upperType = type.toUpperCase();
        return upperType.equals("DEBITED") || upperType.equals("CREDITED");
    }

    /**
     * Validates transaction mode
     */
    public static boolean isValidTransactionMode(String mode) {
        if (mode == null || mode.trim().isEmpty()) {
            return false;
        }
        String upperMode = mode.toUpperCase();
        return upperMode.equals("DEBIT") || upperMode.equals("UPI") ||
                upperMode.equals("CREDIT CARD") || upperMode.equals("CASH") ||
                upperMode.equals("TRANSFER") || upperMode.equals("NEFT") ||
                upperMode.equals("IMPS") || upperMode.equals("RTGS");
    }

    /**
     * Validates account type
     */
    public static boolean isValidAccountType(String type) {
        if (type == null || type.trim().isEmpty()) {
            return false;
        }
        String upperType = type.toUpperCase();
        return upperType.equals("SAVINGS") || upperType.equals("CURRENT") ||
                upperType.equals("FIXED") || upperType.equals("RECURRING");
    }

    /**
     * Validates date of birth - ensures person is at least 18 years old
     */
    public static boolean isValidDOB(java.util.Date dob) {
        if (dob == null) {
            return false;
        }

        java.util.Calendar today = java.util.Calendar.getInstance();
        java.util.Calendar birthDate = java.util.Calendar.getInstance();
        birthDate.setTime(dob);

        int age = today.get(java.util.Calendar.YEAR) - birthDate.get(java.util.Calendar.YEAR);

        if (today.get(java.util.Calendar.DAY_OF_YEAR) < birthDate.get(java.util.Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age >= 18 && age <= 120; // Must be at least 18 and not more than 120
    }

    /**
     * Validates status
     */
    public static boolean isValidStatus(String status) {
        if (status == null || status.trim().isEmpty()) {
            return false;
        }
        String upperStatus = status.toUpperCase();
        return upperStatus.equals("ACTIVE") || upperStatus.equals("INACTIVE") ||
                upperStatus.equals("BLOCKED") || upperStatus.equals("SUSPENDED");
    }

    /**
     * Sanitizes input to prevent SQL injection
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        // Remove any SQL keywords and special characters
        return input.replaceAll("[';\"\\-\\-\\/\\*\\*\\/]", "");
    }

    /**
     * Get validation error message
     */
    public static String getValidationError(String fieldName, String value, String requirement) {
        return String.format("Validation failed for field '%s' with value '%s'. %s",
                fieldName, value, requirement);
    }
}