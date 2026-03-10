package com.dmibiz.bankaccount.repository;

import com.dmibiz.bankaccount.model.Account;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class AccountRepositoryTest {

    @Autowired
    private AccountRepository accountRepository;

    @Test
    void findByIdentification_returnsAccountWhenExists() {
        Account account = Account.builder()
                .identification("1234567")
                .build();
        accountRepository.save(account);

        Optional<Account> result = accountRepository.findByIdentification("1234567");

        assertThat(result)
                .isPresent()
                .get()
                .satisfies(found -> {
                    assertThat(found.getId()).isNotNull();
                    assertThat(found.getIdentification()).isEqualTo("1234567");
                });
    }

    @Test
    void findByIdentification_returnsEmptyWhenNotExists() {
        Optional<Account> result = accountRepository.findByIdentification("4567890");
        assertThat(result).isNotPresent();
    }
}

