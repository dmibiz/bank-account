package com.dmibiz.bankaccount.repository;

import com.dmibiz.bankaccount.model.Account;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void findByIdentification_returnsAccountWhenExists() {
        Account account = Account.builder()
                .identification("123")
                .build();
        accountRepository.save(account);

        Optional<Account> result = accountRepository.findByIdentification("123");

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getId()).isNotNull();
                    assertThat(found.getIdentification()).isEqualTo("123");
                });
    }

    @Test
    void findByIdentification_returnsEmptyWhenNotExists() {
        Optional<Account> result = accountRepository.findByIdentification("456");
        assertThat(result).isNotPresent();
    }
}

