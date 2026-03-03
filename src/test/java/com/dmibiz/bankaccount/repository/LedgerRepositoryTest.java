package com.dmibiz.bankaccount.repository;

import com.dmibiz.bankaccount.model.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LedgerRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LedgerRepository ledgerRepository;

    @Test
    void calculateBalance_returnsCorrectBalance() {
        Account account = Account.builder()
                .identification("123")
                .build();
        account = accountRepository.save(account);

        LedgerEntry credit = LedgerEntry.builder()
                .account(account)
                .currency(Currency.EUR)
                .amount(new BigDecimal("100.00"))
                .type(EntryType.CREDIT)
                .timestamp(LocalDateTime.now())
                .build();

        LedgerEntry debit = LedgerEntry.builder()
                .account(account)
                .currency(Currency.EUR)
                .amount(new BigDecimal("30.00"))
                .type(EntryType.DEBIT)
                .timestamp(LocalDateTime.now())
                .build();

        LedgerEntry creditUsd = LedgerEntry.builder()
                .account(account)
                .currency(Currency.USD)
                .amount(new BigDecimal("50.00"))
                .type(EntryType.CREDIT)
                .timestamp(LocalDateTime.now())
                .build();

        ledgerRepository.save(credit);
        ledgerRepository.save(debit);
        ledgerRepository.save(creditUsd);

        BigDecimal eurBalance = ledgerRepository.calculateBalance(account.getId(), Currency.EUR);
        BigDecimal usdBalance = ledgerRepository.calculateBalance(account.getId(), Currency.USD);

        assertThat(eurBalance).isEqualByComparingTo(new BigDecimal("70.00"));
        assertThat(usdBalance).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    void calculateBalance_returnsZeroWhenNoEntries() {
        Account account = Account.builder()
                .identification("456")
                .build();
        account = accountRepository.save(account);

        BigDecimal eurBalance = ledgerRepository.calculateBalance(account.getId(), Currency.EUR);

        assertThat(eurBalance).isEqualByComparingTo(BigDecimal.ZERO);
    }
}

