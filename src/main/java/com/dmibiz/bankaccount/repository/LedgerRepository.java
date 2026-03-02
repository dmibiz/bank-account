package com.dmibiz.bankaccount.repository;

import com.dmibiz.bankaccount.model.Currency;
import com.dmibiz.bankaccount.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;

public interface LedgerRepository extends JpaRepository<LedgerEntry,Long> {
    @Query("""
                SELECT COALESCE(SUM(
                    CASE
                        WHEN l.type = 'CREDIT' THEN l.amount
                        ELSE -l.amount
                    END
                ), 0)
                FROM LedgerEntry l
                WHERE l.account.id = :accountId
                AND l.currency = :currency
            """)
    BigDecimal calculateBalance(Long accountId, Currency currency);
}
