package com.atm.service;

import com.atm.exception.*;
import com.atm.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;

/**
 * Production-ready implementation of core banking logic for the ATM.
 * Manages customer credentials, thread-safe financial transactions,
 * constraints verification, and ledger logging.
 */
public class BankingServiceImpl implements BankingService {
    public static final BigDecimal DAILY_WITHDRAWAL_CEILING = new BigDecimal("1000.00");

    private final Customer customer;
    private final LedgerService ledgerService;
    private boolean authenticated;

    public BankingServiceImpl() {
        this.customer = new Customer(
                "CUST-89210",
                "Alexander Mercer",
                "4532 8819 0291 8921",
                "1234",
                new BigDecimal("2450.00"),
                new BigDecimal("8900.00")
        );
        this.ledgerService = new LedgerService();
        this.authenticated = false;

        // Seed initial opening balance audit records
        seedInitialRecords();
    }

    private void seedInitialRecords() {
        Account chk = customer.getAccount(AccountType.CHECKING);
        Account sav = customer.getAccount(AccountType.SAVINGS);

        ledgerService.log(new TransactionRecord(
                TransactionType.DEPOSIT,
                AccountType.CHECKING,
                null,
                chk.getBalance(),
                chk.getBalance(),
                "Initial Opening Balance Credit",
                true
        ));

        ledgerService.log(new TransactionRecord(
                TransactionType.DEPOSIT,
                AccountType.SAVINGS,
                null,
                sav.getBalance(),
                sav.getBalance(),
                "Initial Opening Balance Credit",
                true
        ));
    }

    @Override
    public synchronized Customer authenticate(String pin) throws InvalidPinException, AccountLockedException {
        if (customer.isLocked()) {
            throw new AccountLockedException();
        }

        if (pin == null || !pin.matches("\\d{4}")) {
            customer.verifyPin("INVALID"); // counts as a failed attempt
            boolean locked = customer.isLocked();
            int remaining = customer.getRemainingAttempts();
            throw new InvalidPinException(customer.getFailedPinAttempts(), remaining, locked);
        }

        boolean success = customer.verifyPin(pin);
        if (success) {
            authenticated = true;
            ledgerService.log(new TransactionRecord(
                    TransactionType.BALANCE_INQUIRY,
                    null,
                    null,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "Successful PIN Authentication",
                    true
            ));
            return customer;
        } else {
            boolean locked = customer.isLocked();
            int remaining = customer.getRemainingAttempts();
            ledgerService.log(new TransactionRecord(
                    TransactionType.BALANCE_INQUIRY,
                    null,
                    null,
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    "Failed Authentication Attempt (Strike " + customer.getFailedPinAttempts() + "/3)",
                    false
            ));
            throw new InvalidPinException(customer.getFailedPinAttempts(), remaining, locked);
        }
    }

    @Override
    public synchronized BigDecimal getBalance(AccountType accountType) {
        ensureAuthenticated();
        Account account = customer.getAccount(accountType);
        return account != null ? account.getBalance() : BigDecimal.ZERO;
    }

    @Override
    public synchronized BigDecimal getRemainingDailyWithdrawalLimit(AccountType accountType) {
        ensureAuthenticated();
        Account account = customer.getAccount(accountType);
        return account != null ? account.getRemainingDailyLimit() : BigDecimal.ZERO;
    }

    @Override
    public synchronized TransactionRecord deposit(AccountType accountType, BigDecimal amount)
            throws InvalidTransactionException, AccountLockedException {
        ensureAuthenticated();
        checkAccountLock();

        validateAmount(amount);

        Account account = customer.getAccount(accountType);
        if (account == null) {
            throw new InvalidTransactionException("Target account type is invalid: " + accountType);
        }

        synchronized (account) {
            account.deposit(amount);
            TransactionRecord record = new TransactionRecord(
                    TransactionType.DEPOSIT,
                    accountType,
                    null,
                    amount.setScale(2, RoundingMode.HALF_UP),
                    account.getBalance(),
                    String.format("Cash deposit to %s", accountType.getDisplayName()),
                    true
            );
            ledgerService.log(record);
            return record;
        }
    }

    @Override
    public synchronized TransactionRecord withdraw(AccountType accountType, BigDecimal amount)
            throws InsufficientFundsException, DailyLimitExceededException, InvalidTransactionException, AccountLockedException {
        ensureAuthenticated();
        checkAccountLock();

        validateAmount(amount);

        Account account = customer.getAccount(accountType);
        if (account == null) {
            throw new InvalidTransactionException("Target account type is invalid: " + accountType);
        }

        synchronized (account) {
            BigDecimal formattedAmount = amount.setScale(2, RoundingMode.HALF_UP);

            // Business Rule: Enforce available balance constraint
            if (account.getBalance().compareTo(formattedAmount) < 0) {
                TransactionRecord failed = new TransactionRecord(
                        TransactionType.WITHDRAWAL,
                        accountType,
                        null,
                        formattedAmount,
                        account.getBalance(),
                        "Declined: Insufficient funds",
                        false
                );
                ledgerService.log(failed);
                throw new InsufficientFundsException(accountType, formattedAmount, account.getBalance());
            }

            // Business Rule: Enforce $1,000.00 daily withdrawal ceiling
            account.checkAndResetDailyLimit();
            BigDecimal remainingDaily = account.getRemainingDailyLimit();
            if (formattedAmount.compareTo(remainingDaily) > 0) {
                TransactionRecord failed = new TransactionRecord(
                        TransactionType.WITHDRAWAL,
                        accountType,
                        null,
                        formattedAmount,
                        account.getBalance(),
                        "Declined: Daily withdrawal ceiling exceeded",
                        false
                );
                ledgerService.log(failed);
                throw new DailyLimitExceededException(
                        formattedAmount,
                        account.getDailyWithdrawn(),
                        remainingDaily,
                        account.getDailyLimit()
                );
            }

            account.withdraw(formattedAmount);
            TransactionRecord record = new TransactionRecord(
                    TransactionType.WITHDRAWAL,
                    accountType,
                    null,
                    formattedAmount,
                    account.getBalance(),
                    String.format("Cash withdrawal from %s", accountType.getDisplayName()),
                    true
            );
            ledgerService.log(record);
            return record;
        }
    }

    @Override
    public synchronized TransactionRecord[] transfer(AccountType fromAccount, AccountType toAccount, BigDecimal amount)
            throws InsufficientFundsException, InvalidTransactionException, AccountLockedException {
        ensureAuthenticated();
        checkAccountLock();

        if (fromAccount == null || toAccount == null) {
            throw new InvalidTransactionException("Both source and destination accounts must be specified");
        }
        if (fromAccount == toAccount) {
            throw new InvalidTransactionException("Source and destination accounts must be different for a transfer");
        }

        validateAmount(amount);

        Account src = customer.getAccount(fromAccount);
        Account dest = customer.getAccount(toAccount);

        if (src == null || dest == null) {
            throw new InvalidTransactionException("Specified accounts could not be resolved");
        }

        BigDecimal formattedAmount = amount.setScale(2, RoundingMode.HALF_UP);

        // Atomic lock ordering based on enum ordinal to prevent potential deadlocks
        Account firstLock = fromAccount.ordinal() < toAccount.ordinal() ? src : dest;
        Account secondLock = fromAccount.ordinal() < toAccount.ordinal() ? dest : src;

        synchronized (firstLock) {
            synchronized (secondLock) {
                if (src.getBalance().compareTo(formattedAmount) < 0) {
                    TransactionRecord failed = new TransactionRecord(
                            TransactionType.TRANSFER_OUT,
                            fromAccount,
                            toAccount,
                            formattedAmount,
                            src.getBalance(),
                            "Declined: Insufficient transfer funds",
                            false
                    );
                    ledgerService.log(failed);
                    throw new InsufficientFundsException(fromAccount, formattedAmount, src.getBalance());
                }

                // Execute transfer (Transfers do not consume physical ATM cash dispenser limits)
                src.debit(formattedAmount);
                dest.deposit(formattedAmount);

                TransactionRecord outRecord = new TransactionRecord(
                        TransactionType.TRANSFER_OUT,
                        fromAccount,
                        toAccount,
                        formattedAmount,
                        src.getBalance(),
                        String.format("Transfer to %s", toAccount.getDisplayName()),
                        true
                );

                TransactionRecord inRecord = new TransactionRecord(
                        TransactionType.TRANSFER_IN,
                        toAccount,
                        fromAccount,
                        formattedAmount,
                        dest.getBalance(),
                        String.format("Transfer from %s", fromAccount.getDisplayName()),
                        true
                );

                ledgerService.log(outRecord);
                ledgerService.log(inRecord);

                return new TransactionRecord[]{outRecord, inRecord};
            }
        }
    }

    @Override
    public synchronized void changePin(String oldPin, String newPin, String confirmPin)
            throws InvalidPinException, InvalidTransactionException, AccountLockedException {
        ensureAuthenticated();
        checkAccountLock();

        if (oldPin == null || !customer.getPin().equals(oldPin)) {
            throw new InvalidPinException("The current PIN entered is incorrect.");
        }

        if (newPin == null || !newPin.matches("\\d{4}")) {
            throw new InvalidTransactionException("New PIN must be exactly 4 numeric digits (0000 - 9999).");
        }

        if (newPin.equals(oldPin)) {
            throw new InvalidTransactionException("New PIN cannot be the same as your current PIN.");
        }

        if (!newPin.equals(confirmPin)) {
            throw new InvalidTransactionException("New PIN and Confirmation PIN do not match.");
        }

        customer.setPin(newPin);

        TransactionRecord record = new TransactionRecord(
                TransactionType.PIN_CHANGE,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "ATM Customer PIN successfully modified",
                true
        );
        ledgerService.log(record);
    }

    @Override
    public synchronized List<TransactionRecord> getMiniStatement() {
        ensureAuthenticated();
        return ledgerService.getMiniStatement();
    }

    @Override
    public synchronized List<TransactionRecord> getAllTransactions() {
        ensureAuthenticated();
        return ledgerService.getAllRecords();
    }

    @Override
    public synchronized boolean isLocked() {
        return customer.isLocked();
    }

    @Override
    public synchronized int getRemainingAttempts() {
        return customer.getRemainingAttempts();
    }

    @Override
    public synchronized boolean isAuthenticated() {
        return authenticated;
    }

    @Override
    public Customer getCurrentCustomer() {
        return customer;
    }

    @Override
    public synchronized void logout() {
        this.authenticated = false;
        ledgerService.log(new TransactionRecord(
                TransactionType.BALANCE_INQUIRY,
                null,
                null,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                "Customer Session Terminated / Card Ejected",
                true
        ));
    }

    @Override
    public synchronized void unlockCustomer() {
        customer.unlock();
        this.authenticated = false;
    }

    @Override
    public LedgerService getLedgerService() {
        return ledgerService;
    }

    private void ensureAuthenticated() {
        if (!authenticated) {
            throw new IllegalStateException("Unauthorized: No customer is currently authenticated at this ATM.");
        }
    }

    private void checkAccountLock() throws AccountLockedException {
        if (customer.isLocked()) {
            throw new AccountLockedException();
        }
    }

    private void validateAmount(BigDecimal amount) throws InvalidTransactionException {
        if (amount == null) {
            throw new InvalidTransactionException("Transaction amount cannot be empty.");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException("Transaction amount must be strictly greater than $0.00.");
        }
        if (amount.scale() > 2) {
            throw new InvalidTransactionException("Transaction amount cannot have fractional cents beyond 2 decimal places.");
        }
    }
}
