package com.atm.exception;

import com.atm.model.AccountType;
import java.math.BigDecimal;

/**
 * Checked exception thrown when an attempted withdrawal or transfer
 * exceeds the available balance in the specified account.
 */
public class InsufficientFundsException extends ATMException {
    private final AccountType accountType;
    private final BigDecimal requestedAmount;
    private final BigDecimal availableBalance;

    public InsufficientFundsException(AccountType accountType, BigDecimal requestedAmount, BigDecimal availableBalance) {
        super(String.format("Insufficient funds in %s. Requested: $%,.2f, Available Balance: $%,.2f (Deficit: $%,.2f)",
                accountType.getDisplayName(),
                requestedAmount,
                availableBalance,
                requestedAmount.subtract(availableBalance)));
        this.accountType = accountType;
        this.requestedAmount = requestedAmount;
        this.availableBalance = availableBalance;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public BigDecimal getRequestedAmount() {
        return requestedAmount;
    }

    public BigDecimal getAvailableBalance() {
        return availableBalance;
    }
}
