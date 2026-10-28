package com.atm.test;

import com.atm.exception.*;
import com.atm.model.AccountType;
import com.atm.model.Customer;
import com.atm.model.TransactionRecord;
import com.atm.service.BankingService;
import com.atm.service.BankingServiceImpl;

import java.math.BigDecimal;
import java.util.List;

/**
 * Automated test suite verifying core banking rules, multi-account matrix balances,
 * 3-strike security engine, daily withdrawal limits, and custom exceptions.
 */
public class ATMServiceTest {

    public static void main(String[] args) {
        System.out.println("========================================================================");
        System.out.println("           METRO APEX BANK - AUTOMATED TEST SUITE EXECUTION             ");
        System.out.println("========================================================================");

        int passed = 0;
        int failed = 0;

        try {
            testAuthenticationAndSecurityLockout();
            System.out.println("[PASS] testAuthenticationAndSecurityLockout");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] testAuthenticationAndSecurityLockout: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            testDepositBusinessRules();
            System.out.println("[PASS] testDepositBusinessRules");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] testDepositBusinessRules: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            testWithdrawalAndInsufficientFunds();
            System.out.println("[PASS] testWithdrawalAndInsufficientFunds");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] testWithdrawalAndInsufficientFunds: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            testDailyWithdrawalLimitCeiling();
            System.out.println("[PASS] testDailyWithdrawalLimitCeiling");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] testDailyWithdrawalLimitCeiling: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            testCrossAccountTransferUtility();
            System.out.println("[PASS] testCrossAccountTransferUtility");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] testCrossAccountTransferUtility: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            testPinChangeRules();
            System.out.println("[PASS] testPinChangeRules");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] testPinChangeRules: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        try {
            testLedgerAndMiniStatementCap();
            System.out.println("[PASS] testLedgerAndMiniStatementCap");
            passed++;
        } catch (Throwable t) {
            System.err.println("[FAIL] testLedgerAndMiniStatementCap: " + t.getMessage());
            t.printStackTrace();
            failed++;
        }

        System.out.println("========================================================================");
        System.out.printf("TEST SUMMARY: %d PASSED, %d FAILED%n", passed, failed);
        System.out.println("========================================================================");

        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testAuthenticationAndSecurityLockout() throws Exception {
        BankingService service = new BankingServiceImpl();

        // 1. Strike 1
        try {
            service.authenticate("0000");
            throw new AssertionError("Expected InvalidPinException on strike 1");
        } catch (InvalidPinException e) {
            if (e.getAttemptNumber() != 1 || e.getRemainingAttempts() != 2 || e.isLocked()) {
                throw new AssertionError("Strike 1 state invalid: " + e.getMessage());
            }
        }

        // 2. Strike 2
        try {
            service.authenticate("9999");
            throw new AssertionError("Expected InvalidPinException on strike 2");
        } catch (InvalidPinException e) {
            if (e.getAttemptNumber() != 2 || e.getRemainingAttempts() != 1 || e.isLocked()) {
                throw new AssertionError("Strike 2 state invalid: " + e.getMessage());
            }
        }

        // 3. Strike 3 -> Terminal Lockout
        try {
            service.authenticate("1111");
            throw new AssertionError("Expected InvalidPinException with lockout on strike 3");
        } catch (InvalidPinException e) {
            if (e.getAttemptNumber() != 3 || e.getRemainingAttempts() != 0 || !e.isLocked()) {
                throw new AssertionError("Strike 3 lockout state invalid: " + e.getMessage());
            }
        }

        // 4. Subsequent attempt should throw AccountLockedException
        try {
            service.authenticate("1234");
            throw new AssertionError("Expected AccountLockedException when account is locked");
        } catch (AccountLockedException expected) {
            // Success
        }

        // 5. Admin unlock restores access
        service.unlockCustomer();
        if (service.isLocked()) {
            throw new AssertionError("Account should be unlocked after unlockCustomer()");
        }

        Customer cust = service.authenticate("1234");
        if (cust == null || !service.isAuthenticated()) {
            throw new AssertionError("Authentication failed after unlock");
        }
    }

    private static void testDepositBusinessRules() throws Exception {
        BankingService service = new BankingServiceImpl();
        service.authenticate("1234");

        BigDecimal initial = service.getBalance(AccountType.CHECKING);
        TransactionRecord record = service.deposit(AccountType.CHECKING, new BigDecimal("500.00"));

        BigDecimal expected = initial.add(new BigDecimal("500.00"));
        if (service.getBalance(AccountType.CHECKING).compareTo(expected) != 0) {
            throw new AssertionError("Deposit did not credit checking balance correctly");
        }

        // Negative deposit should throw InvalidTransactionException
        try {
            service.deposit(AccountType.CHECKING, new BigDecimal("-50.00"));
            throw new AssertionError("Expected InvalidTransactionException for negative deposit");
        } catch (InvalidTransactionException expectedEx) {
            // Success
        }

        // Zero deposit should throw InvalidTransactionException
        try {
            service.deposit(AccountType.CHECKING, BigDecimal.ZERO);
            throw new AssertionError("Expected InvalidTransactionException for zero deposit");
        } catch (InvalidTransactionException expectedEx) {
            // Success
        }
    }

    private static void testWithdrawalAndInsufficientFunds() throws Exception {
        BankingService service = new BankingServiceImpl();
        service.authenticate("1234");

        BigDecimal initial = service.getBalance(AccountType.CHECKING);
        service.withdraw(AccountType.CHECKING, new BigDecimal("100.00"));

        BigDecimal expected = initial.subtract(new BigDecimal("100.00"));
        if (service.getBalance(AccountType.CHECKING).compareTo(expected) != 0) {
            throw new AssertionError("Withdrawal did not deduct balance correctly");
        }

        // Attempting to withdraw more than available balance throws InsufficientFundsException
        try {
            service.withdraw(AccountType.CHECKING, new BigDecimal("999999.00"));
            throw new AssertionError("Expected InsufficientFundsException when balance is exceeded");
        } catch (InsufficientFundsException expectedEx) {
            // Success
        }
    }

    private static void testDailyWithdrawalLimitCeiling() throws Exception {
        BankingService service = new BankingServiceImpl();
        service.authenticate("1234");

        // Daily limit is $1000.00
        // Withdraw $600.00 -> remaining limit $400.00
        service.withdraw(AccountType.CHECKING, new BigDecimal("600.00"));
        BigDecimal remaining = service.getRemainingDailyWithdrawalLimit(AccountType.CHECKING);
        if (remaining.compareTo(new BigDecimal("400.00")) != 0) {
            throw new AssertionError("Expected $400.00 remaining daily limit, got: " + remaining);
        }

        // Attempting to withdraw $500.00 exceeds remaining $400.00 limit -> DailyLimitExceededException
        try {
            service.withdraw(AccountType.CHECKING, new BigDecimal("500.00"));
            throw new AssertionError("Expected DailyLimitExceededException when daily ceiling is breached");
        } catch (DailyLimitExceededException ex) {
            if (ex.getRemainingDailyLimit().compareTo(new BigDecimal("400.00")) != 0) {
                throw new AssertionError("DailyLimitExceededException remaining limit mismatch");
            }
        }

        // Withdrawing exactly remaining $400.00 succeeds
        service.withdraw(AccountType.CHECKING, new BigDecimal("400.00"));
        if (service.getRemainingDailyWithdrawalLimit(AccountType.CHECKING).compareTo(BigDecimal.ZERO) != 0) {
            throw new AssertionError("Expected $0.00 remaining daily limit");
        }
    }

    private static void testCrossAccountTransferUtility() throws Exception {
        BankingService service = new BankingServiceImpl();
        service.authenticate("1234");

        BigDecimal initChk = service.getBalance(AccountType.CHECKING);
        BigDecimal initSav = service.getBalance(AccountType.SAVINGS);

        // Transfer $500 from Checking to Savings
        TransactionRecord[] records = service.transfer(AccountType.CHECKING, AccountType.SAVINGS, new BigDecimal("500.00"));
        if (records.length != 2) {
            throw new AssertionError("Expected 2 paired transfer records");
        }

        BigDecimal expectedChk = initChk.subtract(new BigDecimal("500.00"));
        BigDecimal expectedSav = initSav.add(new BigDecimal("500.00"));

        if (service.getBalance(AccountType.CHECKING).compareTo(expectedChk) != 0) {
            throw new AssertionError("Checking balance not debited properly in transfer");
        }
        if (service.getBalance(AccountType.SAVINGS).compareTo(expectedSav) != 0) {
            throw new AssertionError("Savings balance not credited properly in transfer");
        }

        // Same account transfer should throw InvalidTransactionException
        try {
            service.transfer(AccountType.CHECKING, AccountType.CHECKING, new BigDecimal("100.00"));
            throw new AssertionError("Expected InvalidTransactionException for identical account transfer");
        } catch (InvalidTransactionException expectedEx) {
            // Success
        }
    }

    private static void testPinChangeRules() throws Exception {
        BankingService service = new BankingServiceImpl();
        service.authenticate("1234");

        // Wrong old PIN
        try {
            service.changePin("9999", "5678", "5678");
            throw new AssertionError("Expected InvalidPinException on incorrect old PIN");
        } catch (InvalidPinException expected) {
            // Success
        }

        // Non-matching confirm PIN
        try {
            service.changePin("1234", "5678", "9999");
            throw new AssertionError("Expected InvalidTransactionException when confirm PIN differs");
        } catch (InvalidTransactionException expected) {
            // Success
        }

        // Successful PIN update
        service.changePin("1234", "4321", "4321");
        service.logout();

        // Old PIN should now fail
        try {
            service.authenticate("1234");
            throw new AssertionError("Old PIN should no longer be accepted");
        } catch (InvalidPinException expected) {
            // Success
        }

        // New PIN authenticates
        service.authenticate("4321");
        if (!service.isAuthenticated()) {
            throw new AssertionError("Failed to authenticate with new PIN");
        }
    }

    private static void testLedgerAndMiniStatementCap() throws Exception {
        BankingService service = new BankingServiceImpl();
        service.authenticate("1234");

        // Execute 7 deposits
        for (int i = 1; i <= 7; i++) {
            service.deposit(AccountType.CHECKING, new BigDecimal("10.00"));
        }

        List<TransactionRecord> mini = service.getMiniStatement();
        if (mini.size() != 5) {
            throw new AssertionError("Mini-statement should be capped at exactly 5 records, got: " + mini.size());
        }

        List<TransactionRecord> all = service.getAllTransactions();
        if (all.size() < 7) {
            throw new AssertionError("Full ledger should capture all events");
        }
    }
}
