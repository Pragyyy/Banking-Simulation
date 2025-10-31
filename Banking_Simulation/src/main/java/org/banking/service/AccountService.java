package org.banking.service;

import org.banking.model.Account;
import java.util.List;

public interface AccountService {
    Account createAccount(Account account) throws Exception;
    Account getAccountById(String accountId) throws Exception;
    Account getAccountByAccountNumber(String accountNumber) throws Exception;
    List<Account> getAccountsByCustomerId(String customerId) throws Exception;
    List<Account> getAccountsByAadhar(String aadharNumber) throws Exception;
    Account updateAccount(String accountNumber, Account account) throws Exception;
    boolean deleteAccount(String accountNumber) throws Exception;
    List<Account> getAllAccounts() throws Exception;
    boolean isAccountNumberExists(String accountNumber) throws Exception;
}