package com.atm.exception;

import java.math.BigDecimal;

/**
 * Checked exception thrown when an attempted cash withdrawal exceeds
 * the statutory $1,000.00 daily withdrawal constraint.
 */
public class DailyLimitExceededException extends ATMException {
    private final BigDecimal attemptedAmount;
    private final BigDecimal currentDailyWithdrawn;
    private final BigDecimal remainingDailyLimit;
    private final BigDecimal dailyLimit;

    public DailyLimitExceededException(BigDecimal attemptedAmount, BigDecimal currentDailyWithdrawn,
                                       BigDecimal remainingDailyLimit, BigDecimal dailyLimit) {
        super(String.format("Daily withdrawal limit exceeded! Attempted: $%,.2f. Already withdrawn today: $%,.2f. Maximum daily limit: $%,.2f (Remaining allowance: $%,.2f)",
                attemptedAmount,
                currentDailyWithdrawn,
                dailyLimit,
                remainingDailyLimit));
        this.attemptedAmount = attemptedAmount;
        this.currentDailyWithdrawn = currentDailyWithdrawn;
        this.remainingDailyLimit = remainingDailyLimit;
        this.dailyLimit = dailyLimit;
    }

    public BigDecimal getAttemptedAmount() {
        return attemptedAmount;
    }

    public BigDecimal getCurrentDailyWithdrawn() {
        return currentDailyWithdrawn;
    }

    public BigDecimal getRemainingDailyLimit() {
        return remainingDailyLimit;
    }

    public BigDecimal getDailyLimit() {
        return dailyLimit;
    }
}
