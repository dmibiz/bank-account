package com.dmibiz.bankaccount.dto;

import com.dmibiz.bankaccount.model.Currency;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class CreditDebitRequest {
    @NotNull
    private Currency currency;

    @NotNull
    @Positive
    private BigDecimal amount;
}
