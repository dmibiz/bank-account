package com.dmibiz.bankaccount.service;

import com.dmibiz.bankaccount.exception.AccountNotFoundException;
import com.dmibiz.bankaccount.exception.InsufficientFundsException;
import com.dmibiz.bankaccount.model.Account;
import com.dmibiz.bankaccount.model.Currency;
import com.dmibiz.bankaccount.model.EntryType;
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
    private final ExchangeService exchangeService;
    private final ExternalLoggingService externalLoggingService;

    public Account createAccount(String identification) {
       return accountRepository.save(Account.builder()
               .identification(identification)
               .build());
    }

    public BigDecimal getBalance(String accountIdentification, Currency currency) {
        Account account = getAccountByIdentification(accountIdentification);
        return ledgerRepository.calculateBalance(account.getId(), currency);
    }

    public void credit(String accountIdentification, Currency currency, BigDecimal amount) {
        Account account = getAccountByIdentification(accountIdentification);
        ledgerRepository.save(LedgerEntry.builder()
                .account(account)
                .currency(currency)
                .amount(amount)
                .type(EntryType.CREDIT)
                .timestamp(LocalDateTime.now())
                .build());
    }

    public void debit(String accountIdentification, Currency currency, BigDecimal amount) {
        BigDecimal balance = getBalance(accountIdentification, currency);

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(accountIdentification);
        }

        Account account = getAccountByIdentification(accountIdentification);

        ledgerRepository.save(LedgerEntry.builder()
                .account(account)
                .currency(currency)
                .amount(amount)
                .type(EntryType.DEBIT)
                .timestamp(LocalDateTime.now())
                .build());

        externalLoggingService.logDebit(); // to simulate a call to an external system
    }


    private Account getAccountByIdentification(String identification) {
        return accountRepository.findByIdentification(identification)
                .orElseThrow(() -> new AccountNotFoundException(identification));
    }

    public void exchange(String accountIdentification, Currency from, Currency to, BigDecimal amount) {
        BigDecimal balance = getBalance(accountIdentification, from);

        if (balance.compareTo(amount) < 0) {
            throw new InsufficientFundsException(accountIdentification);
        }

        BigDecimal converted = exchangeService.convert(from, to, amount);

        debit(accountIdentification, from, amount);
        credit(accountIdentification, to, converted);
    }
}
