package com.dmibiz.bankaccount.controller;

import com.dmibiz.bankaccount.model.Account;
import com.dmibiz.bankaccount.model.Currency;
import com.dmibiz.bankaccount.service.AccountService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AccountControllerTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void createAccount_createsAndReturnsAccount() throws Exception {
        Account account = Account.builder()
                .id(1L)
                .identification("1234567")
                .build();

        when(accountService.createAccount("1234567")).thenReturn(account);

        String jsonBody = "{\"identification\":\"1234567\"}";

        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        verify(accountService).createAccount("1234567");
    }

    @Test
    void credit_delegatesToService() throws Exception {
        String jsonBody = "{\"currency\":\"EUR\",\"amount\":100.00}";

        mockMvc.perform(post("/accounts/1234567/credit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        verify(accountService).credit("1234567", Currency.EUR, new BigDecimal("100.00"));
    }

    @Test
    void debit_delegatesToService() throws Exception {
        String jsonBody = "{\"currency\":\"EUR\",\"amount\":50.00}";

        mockMvc.perform(post("/accounts/1234567/debit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        verify(accountService).debit("1234567", Currency.EUR, new BigDecimal("50.00"));
    }

    @Test
    void exchange_delegatesToService() throws Exception {
        String jsonBody = "{\"from\":\"EUR\",\"to\":\"USD\",\"amount\":10.00}";

        mockMvc.perform(post("/accounts/1234567/exchange")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk());

        verify(accountService).exchange("1234567", Currency.EUR, Currency.USD, new BigDecimal("10.00"));
    }

    @Test
    void balance_returnsServiceBalance() throws Exception {
        when(accountService.getBalance("1234567", Currency.EUR))
                .thenReturn(new BigDecimal("123.45"));

        mockMvc.perform(get("/accounts/1234567/balance")
                        .param("currency", "EUR")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"currency\":\"EUR\",\"amount\":123.45}"));

        verify(accountService).getBalance("1234567", Currency.EUR);
    }
}
