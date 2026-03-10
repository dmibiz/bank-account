package com.dmibiz.bankaccount.service;

import com.dmibiz.bankaccount.model.Account;
import com.dmibiz.bankaccount.model.Currency;
import com.dmibiz.bankaccount.model.EntryType;
import com.dmibiz.bankaccount.model.LedgerEntry;
import com.dmibiz.bankaccount.repository.AccountRepository;
import com.dmibiz.bankaccount.repository.LedgerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private LedgerRepository ledgerRepository;

    @Mock
    private ExchangeService exchangeService;

    @Mock
    private ExternalLoggingService externalLoggingService;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_shouldPersistAndReturnAccount() {
        String identification = "1234567";
        Account saved = Account.builder().id(1L).identification(identification).build();

        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        Account result = accountService.createAccount(identification);

        assertEquals(saved, result);
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void getBalance_shouldDelegateToLedgerRepository() {
        String identification = "1234567";
        Currency currency = Currency.EUR;
        Account account = Account.builder().id(1L).identification(identification).build();

        when(accountRepository.findByIdentification(identification)).thenReturn(Optional.of(account));
        when(ledgerRepository.calculateBalance(1L, currency)).thenReturn(new BigDecimal("150.00"));

        BigDecimal balance = accountService.getBalance(identification, currency);

        assertEquals(new BigDecimal("150.00"), balance);
        verify(accountRepository).findByIdentification(identification);
        verify(ledgerRepository).calculateBalance(1L, currency);
    }

    @Test
    void credit_shouldCreateCreditLedgerEntry() {
        String identification = "1234567";
        Currency currency = Currency.EUR;
        BigDecimal amount = new BigDecimal("100.00");
        Account account = Account.builder().id(1L).identification(identification).build();

        when(accountRepository.findByIdentification(identification)).thenReturn(Optional.of(account));

        accountService.credit(identification, currency, amount);

        ArgumentCaptor<LedgerEntry> entryCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepository).save(entryCaptor.capture());

        LedgerEntry savedEntry = entryCaptor.getValue();
        assertEquals(account, savedEntry.getAccount());
        assertEquals(currency, savedEntry.getCurrency());
        assertEquals(amount, savedEntry.getAmount());
        assertEquals(EntryType.CREDIT, savedEntry.getType());
    }

    @Test
    void debit_shouldThrowWhenInsufficientFunds() {
        String identification = "1234567";
        Currency currency = Currency.EUR;
        BigDecimal amount = new BigDecimal("100.00");
        Account account = Account.builder().id(1L).identification(identification).build();

        when(accountRepository.findByIdentification(identification)).thenReturn(Optional.of(account));
        when(ledgerRepository.calculateBalance(1L, currency)).thenReturn(new BigDecimal("50.00"));

        assertThrows(RuntimeException.class, () -> accountService.debit(identification, currency, amount));

        verify(externalLoggingService).logDebit();
    }

    @Test
    void debit_shouldCreateDebitLedgerEntryWhenEnoughBalance() {
        String identification = "1234567";
        Currency currency = Currency.EUR;
        BigDecimal amount = new BigDecimal("50.00");
        Account account = Account.builder().id(1L).identification(identification).build();

        when(accountRepository.findByIdentification(identification)).thenReturn(Optional.of(account));
        when(ledgerRepository.calculateBalance(1L, currency)).thenReturn(new BigDecimal("100.00"));

        accountService.debit(identification, currency, amount);

        verify(externalLoggingService).logDebit();

        ArgumentCaptor<LedgerEntry> entryCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepository).save(entryCaptor.capture());

        LedgerEntry savedEntry = entryCaptor.getValue();
        assertEquals(account, savedEntry.getAccount());
        assertEquals(currency, savedEntry.getCurrency());
        assertEquals(amount, savedEntry.getAmount());
        assertEquals(EntryType.DEBIT, savedEntry.getType());
    }

    @Test
    void exchange_shouldDebitFromSourceAndCreditToTarget() {
        String identification = "1234567";
        Currency from = Currency.EUR;
        Currency to = Currency.USD;
        BigDecimal amount = new BigDecimal("10.00");
        BigDecimal converted = new BigDecimal("11.50");
        Account account = Account.builder().id(1L).identification(identification).build();

        when(accountRepository.findByIdentification(identification)).thenReturn(Optional.of(account));
        when(ledgerRepository.calculateBalance(1L, from)).thenReturn(new BigDecimal("100.00"));
        when(exchangeService.convert(from, to, amount)).thenReturn(converted);

        accountService.exchange(identification, from, to, amount);

        // two ledger entries: one debit (from), one credit (to)
        ArgumentCaptor<LedgerEntry> entryCaptor = ArgumentCaptor.forClass(LedgerEntry.class);
        verify(ledgerRepository, times(2)).save(entryCaptor.capture());

        LedgerEntry first = entryCaptor.getAllValues().get(0);
        LedgerEntry second = entryCaptor.getAllValues().get(1);

        // debit entry
        assertEquals(EntryType.DEBIT, first.getType());
        assertEquals(from, first.getCurrency());
        assertEquals(amount, first.getAmount());

        // credit entry
        assertEquals(EntryType.CREDIT, second.getType());
        assertEquals(to, second.getCurrency());
        assertEquals(converted, second.getAmount());

        verify(exchangeService).convert(from, to, amount);
    }
}
