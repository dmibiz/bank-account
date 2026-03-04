package com.dmibiz.bankaccount.controller;

import com.dmibiz.bankaccount.model.Account;
import com.dmibiz.bankaccount.model.Currency;
import com.dmibiz.bankaccount.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void createAccount_createsAndReturnsAccount() throws Exception {
        Account account = Account.builder()
                .id(1L)
                .identification("123")
                .build();

        when(accountService.createAccount("123")).thenReturn(account);

        mockMvc.perform(post("/accounts")
                        .param("identification", "123")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(accountService).createAccount("123");
    }

    @Test
    void credit_delegatesToService() throws Exception {
        mockMvc.perform(post("/accounts/123/credit")
                        .param("currency", "EUR")
                        .param("amount", "100.00"))
                .andExpect(status().isOk());

        verify(accountService).credit("123", Currency.EUR, new BigDecimal("100.00"));
    }

    @Test
    void debit_delegatesToService() throws Exception {
        mockMvc.perform(post("/accounts/123/debit")
                        .param("currency", "EUR")
                        .param("amount", "50.00"))
                .andExpect(status().isOk());

        verify(accountService).debit("123", Currency.EUR, new BigDecimal("50.00"));
    }

    @Test
    void exchange_delegatesToService() throws Exception {
        mockMvc.perform(post("/accounts/123/exchange")
                        .param("from", "EUR")
                        .param("to", "USD")
                        .param("amount", "10.00"))
                .andExpect(status().isOk());

        verify(accountService).exchange("123", Currency.EUR, Currency.USD, new BigDecimal("10.00"));
    }

    @Test
    void balance_returnsServiceBalance() throws Exception {
        when(accountService.getBalance("123", Currency.EUR))
                .thenReturn(new BigDecimal("123.45"));

        mockMvc.perform(get("/accounts/123/balance")
                        .param("currency", "EUR")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("123.45"));

        verify(accountService).getBalance("123", Currency.EUR);
    }
}

