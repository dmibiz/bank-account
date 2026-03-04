package com.dmibiz.bankaccount.controller;

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
    public Account create(@RequestParam String identification) {
        return accountService.createAccount(identification);
    }

    @PostMapping("/{identification}/credit")
    public void credit(@PathVariable String identification,
                       @RequestParam Currency currency,
                       @RequestParam BigDecimal amount) {
        accountService.credit(identification, currency, amount);
    }

    @PostMapping("/{identification}/debit")
    public void debit(@PathVariable String identification,
                      @RequestParam Currency currency,
                      @RequestParam BigDecimal amount) {
        accountService.debit(identification, currency, amount);
    }

    @PostMapping("/{identification}/exchange")
    public void exchange(@PathVariable String identification,
                         @RequestParam Currency from,
                         @RequestParam Currency to,
                         @RequestParam BigDecimal amount) {
        accountService.exchange(identification, from, to, amount);
    }

    @GetMapping("/{identification}/balance")
    public BigDecimal balance(@PathVariable String identification,
                              @RequestParam Currency currency) {
        return accountService.getBalance(identification, currency);
    }
}
