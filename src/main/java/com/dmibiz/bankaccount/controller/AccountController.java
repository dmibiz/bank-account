package com.dmibiz.bankaccount.controller;

import com.dmibiz.bankaccount.model.Account;
import com.dmibiz.bankaccount.model.Currency;
import com.dmibiz.bankaccount.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public Account create(@RequestParam String identification) {
        return accountService.createAccount(identification);
    }

    @PostMapping("/{id}/credit")
    public void credit(@PathVariable Long id,
                       @RequestParam Currency currency,
                       @RequestParam BigDecimal amount) {
        accountService.credit(id, currency, amount);
    }

    @PostMapping("/{id}/debit")
    public void debit(@PathVariable Long id,
                      @RequestParam Currency currency,
                      @RequestParam BigDecimal amount) {
        accountService.debit(id, currency, amount);
    }

    @PostMapping("/{id}/exchange")
    public void exchange(@PathVariable Long id,
                         @RequestParam Currency from,
                         @RequestParam Currency to,
                         @RequestParam BigDecimal amount) {
        accountService.exchange(id, from, to, amount);
    }

    @GetMapping("/{id}/balance")
    public BigDecimal balance(@PathVariable Long id,
                              @RequestParam Currency currency) {
        return accountService.getBalance(id, currency);
    }
}
