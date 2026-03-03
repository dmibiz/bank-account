package com.dmibiz.bankaccount.repository;

import com.dmibiz.bankaccount.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIdentification(String identification);
}
