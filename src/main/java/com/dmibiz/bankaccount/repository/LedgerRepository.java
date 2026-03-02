package com.dmibiz.bankaccount.repository;

import com.dmibiz.bankaccount.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerRepository extends JpaRepository<LedgerEntry,Long> {
}
