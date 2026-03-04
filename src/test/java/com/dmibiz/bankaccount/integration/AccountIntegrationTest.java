package com.dmibiz.bankaccount.integration;

import com.dmibiz.bankaccount.model.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

// This needs Docker environment to run

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AccountIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18-alpine")
            .withDatabaseName("bankaccount")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        // Let Hibernate create/drop schema for tests
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @LocalServerPort
    private int port;

    @Autowired
    private RestClient restClient;

    private String baseUrl(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void fullFlow_creditDebitBalance() {
        // 1) Create account
        String identification = "123";
        ResponseEntity<String> createResponse = restClient.post()
                .uri(baseUrl("/accounts?identification=" + identification))
                .retrieve()
                .toEntity(String.class);

        assertThat(createResponse.getStatusCode().is2xxSuccessful()).isTrue();

        // 2) Credit 100 EUR
        ResponseEntity<Void> creditResponse = restClient.post()
                .uri(baseUrl("/accounts/" + identification + "/credit?currency=" + Currency.EUR + "&amount=100.00"))
                .retrieve()
                .toBodilessEntity();
        assertThat(creditResponse.getStatusCode().is2xxSuccessful()).isTrue();

        // 3) Debit 40 EUR
        ResponseEntity<Void> debitResponse = restClient.post()
                .uri(baseUrl("/accounts/" + identification + "/debit?currency=" + Currency.EUR + "&amount=40.00"))
                .retrieve()
                .toBodilessEntity();
        assertThat(debitResponse.getStatusCode().is2xxSuccessful()).isTrue();

        // 4) Check EUR balance = 60.00
        ResponseEntity<String> balanceResponse = restClient.get()
                .uri(baseUrl("/accounts/" + identification + "/balance?currency=" + Currency.EUR))
                .retrieve()
                .toEntity(String.class);

        assertThat(balanceResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(new BigDecimal(balanceResponse.getBody()))
                .isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void fullFlow_exchange() {
        String identification = "123";

        // create account
        restClient.post()
                .uri(baseUrl("/accounts?identification=" + identification))
                .retrieve()
                .toBodilessEntity();

        // credit some EUR
        restClient.post()
                .uri(baseUrl("/accounts/" + identification + "/credit?currency=" + Currency.EUR + "&amount=100.00"))
                .retrieve()
                .toBodilessEntity();

        // capture balances before
        BigDecimal eurBefore = new BigDecimal(restClient.get()
                .uri(baseUrl("/accounts/" + identification + "/balance?currency=" + Currency.EUR))
                .retrieve()
                .body(String.class));
        BigDecimal usdBefore = new BigDecimal(restClient.get()
                .uri(baseUrl("/accounts/" + identification + "/balance?currency=" + Currency.USD))
                .retrieve()
                .body(String.class));

        // perform exchange 20 EUR -> USD
        ResponseEntity<Void> exchangeResponse = restClient.post()
                .uri(baseUrl("/accounts/" + identification + "/exchange?from=" + Currency.EUR + "&to=" + Currency.USD + "&amount=20.00"))
                .retrieve()
                .toBodilessEntity();
        assertThat(exchangeResponse.getStatusCode().is2xxSuccessful()).isTrue();

        // balances after
        BigDecimal eurAfter = new BigDecimal(restClient.get()
                .uri(baseUrl("/accounts/" + identification + "/balance?currency=" + Currency.EUR))
                .retrieve()
                .body(String.class));
        BigDecimal usdAfter = new BigDecimal(restClient.get()
                .uri(baseUrl("/accounts/" + identification + "/balance?currency=" + Currency.USD))
                .retrieve()
                .body(String.class));

        assertThat(eurAfter).isLessThan(eurBefore);
        assertThat(usdAfter).isGreaterThanOrEqualTo(usdBefore);
    }

    @Test
    void debit_insufficientFundsReturnsError() {
        String identification = "123";

        // create account without crediting
        restClient.post()
                .uri(baseUrl("/accounts?identification=" + identification))
                .retrieve()
                .toBodilessEntity();

        ResponseEntity<String> response = restClient.method(HttpMethod.POST)
                .uri(baseUrl("/accounts/" + identification + "/debit?currency=" + Currency.EUR + "&amount=10.00"))
                .retrieve()
                .toEntity(String.class);

        assertThat(response.getStatusCode().is5xxServerError()).isTrue();
    }
}

