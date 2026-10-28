package com.atm.service;

import com.atm.exception.*;
import com.atm.model.AccountType;
import com.atm.model.Customer;
import com.atm.model.TransactionRecord;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service contract for core banking ATM business logic, decoupling financial operations
 * from the GUI presentation layer.
 */
public interface BankingService {

    /**
     * Authenticates customer PIN with 3-strike safe security enforcement.
     */
    Customer authenticate(String pin) throws InvalidPinException, AccountLockedException;

    /**
     * Retrieves current balance for the given account type.
     */
    BigDecimal getBalance(AccountType accountType);

    /**
     * Retrieves remaining daily withdrawal quota ($1,000.00 ceiling).
     */
    BigDecimal getRemainingDailyWithdrawalLimit(AccountType accountType);

    /**
     * Executes cash deposit into the selected account.
     */
    TransactionRecord deposit(AccountType accountType, BigDecimal amount)
            throws InvalidTransactionException, AccountLockedException;

    /**
     * Executes cash withdrawal under balance and daily limit constraints.
     */
    TransactionRecord withdraw(AccountType accountType, BigDecimal amount)
            throws InsufficientFundsException, DailyLimitExceededException, InvalidTransactionException, AccountLockedException;

    /**
     * Executes atomic cross-allocation transfer between Checking and Savings.
     */
    TransactionRecord[] transfer(AccountType fromAccount, AccountType toAccount, BigDecimal amount)
            throws InsufficientFundsException, InvalidTransactionException, AccountLockedException;

    /**
     * Updates customer PIN with validation.
     */
    void changePin(String oldPin, String newPin, String confirmPin)
            throws InvalidPinException, InvalidTransactionException, AccountLockedException;

    /**
     * Returns the last 5 transaction records.
     */
    List<TransactionRecord> getMiniStatement();

    /**
     * Returns full transaction ledger.
     */
    List<TransactionRecord> getAllTransactions();

    /**
     * Checks if current customer or terminal state is locked.
     */
    boolean isLocked();

    /**
     * Gets current remaining attempts before lockout.
     */
    int getRemainingAttempts();

    /**
     * Checks if a user is currently authenticated in the session.
     */
    boolean isAuthenticated();

    /**
     * Returns current customer domain model.
     */
    Customer getCurrentCustomer();

    /**
     * Terminates active session.
     */
    void logout();

    /**
     * Administrative recovery utility to reset lockout state.
     */
    void unlockCustomer();

    /**
     * Provides access to underlying ledger service.
     */
    LedgerService getLedgerService();
}
