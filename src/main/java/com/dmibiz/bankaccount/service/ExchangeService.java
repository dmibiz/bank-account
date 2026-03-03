package com.dmibiz.bankaccount.service;

import com.dmibiz.bankaccount.model.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class ExchangeService {

    private static final Map<Currency, BigDecimal> EUR_BASE_RATES = Map.of(
            Currency.EUR, BigDecimal.ONE,
            Currency.USD, new BigDecimal("1.10"),
            Currency.SEK, new BigDecimal("11.00"),
            Currency.GBP, new BigDecimal("0.85")
    );

    public BigDecimal convert(Currency from, Currency to, BigDecimal amount) {
        BigDecimal amountInEur = amount.divide(EUR_BASE_RATES.get(from), 4, RoundingMode.HALF_UP);
        return amountInEur.multiply(EUR_BASE_RATES.get(to));
    }
}
