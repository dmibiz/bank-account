package com.dmibiz.bankaccount.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String identification) {
        super("Account not found with identification: " + identification);
    }
}
