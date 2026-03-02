package com.dmibiz.bankaccount.repository;

import com.dmibiz.bankaccount.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<Account, Long> {
}
