package org.banking.service;

import org.banking.model.Account;
import org.banking.repository.AccountRepository;
import org.banking.repository.AccountRepositoryImpl;
import java.time.LocalDateTime;
import java.util.List;

public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    // Default constructor for production use
    public AccountServiceImpl() {
        this.accountRepository = new AccountRepositoryImpl();
    }

    // Constructor for testing (allows dependency injection)
    public AccountServiceImpl(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Account createAccount(Account account) throws Exception {
        try {
            // Get customer_id from aadhar_number
            String customerId = accountRepository.getCustomerIdByAadhar(account.getAadharNumber());

            if (customerId == null) {
                throw new Exception("Customer not found with Aadhar number: " + account.getAadharNumber());
            }

            // Set the customer_id in the account object
            account.setCustomerId(customerId);

            // Generate account ID
            int nextId = accountRepository.getNextId();
            String accountId = String.format("ACC_%06d", nextId);
            account.setAccountId(accountId);

            // Set timestamps
            LocalDateTime now = LocalDateTime.now();
            account.setCreatedAt(now);
            account.setModifiedAt(now);

            // Default values
            if (account.getBalance() == null) {
                account.setBalance(50.00);
            }

            if (account.getStatus() == null || account.getStatus().isEmpty()) {
                account.setStatus("Active");
            }

            return accountRepository.save(account);
        } catch (Exception e) {
            throw new Exception("Failed to create account: " + e.getMessage(), e);
        }
    }

    @Override
    public Account getAccountById(String accountId) throws Exception {
        try {
            return accountRepository.findById(accountId);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve account: " + e.getMessage(), e);
        }
    }

    @Override
    public Account getAccountByAccountNumber(String accountNumber) throws Exception {
        try {
            return accountRepository.findByAccountNumber(accountNumber);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve account: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> getAccountsByCustomerId(String customerId) throws Exception {
        try {
            return accountRepository.findByCustomerId(customerId);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve accounts: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> getAccountsByAadhar(String aadharNumber) throws Exception {
        try {
            String customerId = accountRepository.getCustomerIdByAadhar(aadharNumber);
            if (customerId == null) {
                throw new Exception("Customer not found with Aadhar number: " + aadharNumber);
            }
            return accountRepository.findByCustomerId(customerId);
        } catch (Exception e) {
            throw new Exception("Failed to retrieve accounts: " + e.getMessage(), e);
        }
    }

    @Override
    public Account updateAccount(String accountNumber, Account account) throws Exception {
        try {
            Account existing = accountRepository.findByAccountNumber(accountNumber);
            if (existing == null) {
                throw new Exception("Account not found with Account Number: " + accountNumber);
            }

            Account updated = accountRepository.update(accountNumber, account);
            if (updated == null) {
                throw new Exception("Failed to update account");
            }
            return updated;
        } catch (Exception e) {
            throw new Exception("Failed to update account: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteAccount(String accountNumber) throws Exception {
        try {
            return accountRepository.deleteByAccountNumber(accountNumber);
        } catch (Exception e) {
            throw new Exception("Failed to delete account: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Account> getAllAccounts() throws Exception {
        try {
            return accountRepository.findAll();
        } catch (Exception e) {
            throw new Exception("Failed to retrieve accounts: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean isAccountNumberExists(String accountNumber) throws Exception {
        try {
            return accountRepository.existsByAccountNumber(accountNumber);
        } catch (Exception e) {
            throw new Exception("Failed to check account number: " + e.getMessage(), e);
        }
    }
}