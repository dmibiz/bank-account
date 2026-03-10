package com.dmibiz.bankaccount.dto;


import com.dmibiz.bankaccount.model.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ExchangeRequest {
    @NotNull
    private Currency from;

    @NotNull
    private Currency to;

    @NotNull
    @Positive
    private BigDecimal amount;
}
