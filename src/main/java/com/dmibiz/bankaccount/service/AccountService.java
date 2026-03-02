package com.dmibiz.bankaccount.service;

import com.dmibiz.bankaccount.model.Account;
import com.dmibiz.bankaccount.model.Currency;
import com.dmibiz.bankaccount.model.LedgerEntry;
import com.dmibiz.bankaccount.repository.AccountRepository;
import com.dmibiz.bankaccount.repository.LedgerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;
    private final ExternalLoggingService externalLoggingService;

    public Account createAccount(String identification) {
       return accountRepository.save(Account.builder()
               .identification(identification)
               .build());
    }

    public BigDecimal getBalance(Long accountId, Currency currency) {
        return ledgerRepository.calculateBalance(accountId, currency);
    }

    public void credit(Long accountId, Currency currency, BigDecimal amount) {
        Account account = getAccountById(accountId);
        ledgerRepository.save(LedgerEntry.builder()
                .account(account)
                .currency(currency)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .build());
    }

    public void debit(Long accountId, Currency currency, BigDecimal amount) {
        externalLoggingService.logDebit(); // to simulate a call to an external system
        BigDecimal balance = getBalance(accountId, currency);

        if (balance.compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        Account account = getAccountById(accountId);

        ledgerRepository.save(LedgerEntry.builder()
                .account(account)
                .currency(currency)
                .amount(amount)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private Account getAccountById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + id));
    }
}
