package com.dmibiz.bankaccount.controller;

import com.dmibiz.bankaccount.dto.*;
import com.dmibiz.bankaccount.model.Account;
import com.dmibiz.bankaccount.model.Currency;
import com.dmibiz.bankaccount.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public AccountResponse create(@RequestBody CreateAccountRequest createAccountRequest) {
        Account account = accountService.createAccount(createAccountRequest.getIdentification());
        return new AccountResponse(account.getIdentification());
    }

    @PostMapping("/{identification}/credit")
    public void credit(@PathVariable String identification,
                       @RequestBody CreditDebitRequest creditDebitRequest) {
        accountService.credit(identification, creditDebitRequest.getCurrency(), creditDebitRequest.getAmount());
    }

    @PostMapping("/{identification}/debit")
    public void debit(@PathVariable String identification,
                      @RequestBody CreditDebitRequest request) {
        accountService.debit(identification, request.getCurrency(), request.getAmount());
    }

    @PostMapping("/{identification}/exchange")
    public void exchange(@PathVariable String identification,
                         @RequestBody ExchangeRequest exchangeRequest) {
        accountService.exchange(identification, exchangeRequest.getFrom(), exchangeRequest.getTo(), exchangeRequest.getAmount());
    }

    @GetMapping("/{identification}/balance")
    public BigDecimal balance(@PathVariable String identification,
                              @RequestParam Currency currency) {
        return accountService.getBalance(identification, currency);
    }
}
