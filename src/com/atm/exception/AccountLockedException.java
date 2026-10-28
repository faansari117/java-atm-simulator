package com.atm.exception;

/**
 * Checked exception thrown when an operation is attempted against a locked account
 * or terminal state that has been disabled due to 3-strike security enforcement.
 */
public class AccountLockedException extends ATMException {
    public AccountLockedException() {
        super("SECURITY LOCKOUT: Your card and account access have been locked due to multiple invalid PIN entries. Please contact branch customer support to restore access.");
    }

    public AccountLockedException(String customMessage) {
        super(customMessage);
    }
}
