package com.dmibiz.bankaccount.exception;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException(String identification) {
        super("Insufficient funds for account: " + identification);
    }
}
