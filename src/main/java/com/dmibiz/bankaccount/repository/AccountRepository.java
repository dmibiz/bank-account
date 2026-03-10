package com.dmibiz.bankaccount.repository;

import com.dmibiz.bankaccount.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByIdentification(String identification);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findWithLockByIdentification(String identification);
}
