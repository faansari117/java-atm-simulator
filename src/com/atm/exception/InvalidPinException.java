package com.atm.exception;

/**
 * Checked exception thrown when an invalid PIN is supplied during authentication
 * or security-critical operations, capturing strike counts and lockout status.
 */
public class InvalidPinException extends ATMException {
    private final int attemptNumber;
    private final int remainingAttempts;
    private final boolean locked;

    public InvalidPinException(int attemptNumber, int remainingAttempts, boolean locked) {
        super(locked
                ? "SECURITY LOCKOUT: Maximum PIN attempts (3) exceeded. Access has been locked."
                : String.format("Invalid PIN entered. %d attempt(s) remaining.", remainingAttempts));
        this.attemptNumber = attemptNumber;
        this.remainingAttempts = remainingAttempts;
        this.locked = locked;
    }

    public InvalidPinException(String customMessage) {
        super(customMessage);
        this.attemptNumber = 0;
        this.remainingAttempts = 0;
        this.locked = false;
    }

    public int getAttemptNumber() {
        return attemptNumber;
    }

    public int getRemainingAttempts() {
        return remainingAttempts;
    }

    public boolean isLocked() {
        return locked;
    }
}
