package com.dmibiz.bankaccount.integration;

import com.dmibiz.bankaccount.dto.ErrorResponse;
import com.dmibiz.bankaccount.dto.Money;
import com.dmibiz.bankaccount.model.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class AccountIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("bankaccount")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureDatasource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @LocalServerPort
    private int port;

    private RestClient restClient;

    @BeforeEach
    void setupRestClient() {
        restClient = RestClient.builder()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    private String baseUrl(String path) {
        return path; // baseUrl is already set on the RestClient builder
    }

    @Test
    void fullFlow_creditDebitBalance() {
        String identification = "1234567";

        String createJson = "{\"identification\":\"" + identification + "\"}";
        ResponseEntity<String> createResponse = restClient.post()
                .uri(baseUrl("/accounts"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(createJson)
                .retrieve()
                .toEntity(String.class);

        assertThat(createResponse.getStatusCode().is2xxSuccessful()).isTrue();

        String creditJson = "{\"currency\":\"EUR\",\"amount\":100.00}";
        ResponseEntity<Void> creditResponse = restClient.post()
                .uri(baseUrl("/accounts/" + identification + "/credit"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(creditJson)
                .retrieve()
                .toBodilessEntity();
        assertThat(creditResponse.getStatusCode().is2xxSuccessful()).isTrue();

        String debitJson = "{\"currency\":\"EUR\",\"amount\":40.00}";
        ResponseEntity<Void> debitResponse = restClient.post()
                .uri(baseUrl("/accounts/" + identification + "/debit"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(debitJson)
                .retrieve()
                .toBodilessEntity();
        assertThat(debitResponse.getStatusCode().is2xxSuccessful()).isTrue();

        ResponseEntity<Money> balanceResponse = restClient.get()
            .uri(baseUrl("/accounts/" + identification + "/balance?currency=" + Currency.EUR))
            .retrieve()
            .toEntity(Money.class);

        assertThat(balanceResponse.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(balanceResponse.getBody()).isNotNull();
        assertThat(balanceResponse.getBody().getAmount())
                .isEqualByComparingTo(new BigDecimal("60.00"));
    }

    @Test
    void fullFlow_exchange() {
        String identification = "7654321";

        String createJson = "{\"identification\":\"" + identification + "\"}";
        restClient.post()
                .uri(baseUrl("/accounts"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(createJson)
                .retrieve()
                .toBodilessEntity();

        String creditJson = "{\"currency\":\"EUR\",\"amount\":100.00}";
        restClient.post()
                .uri(baseUrl("/accounts/" + identification + "/credit"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(creditJson)
                .retrieve()
                .toBodilessEntity();

        BigDecimal eurBefore = restClient.get()
                .uri(baseUrl("/accounts/" + identification + "/balance?currency=" + Currency.EUR))
                .retrieve()
                .body(Money.class)
                .getAmount();
        BigDecimal usdBefore = restClient.get()
                .uri(baseUrl("/accounts/" + identification + "/balance?currency=" + Currency.USD))
                .retrieve()
                .body(Money.class)
                .getAmount();

        String exchangeJson = "{\"from\":\"EUR\",\"to\":\"USD\",\"amount\":20.00}";
        ResponseEntity<Void> exchangeResponse = restClient.post()
                .uri(baseUrl("/accounts/" + identification + "/exchange"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(exchangeJson)
                .retrieve()
                .toBodilessEntity();
        assertThat(exchangeResponse.getStatusCode().is2xxSuccessful()).isTrue();

        BigDecimal eurAfter = restClient.get()
                .uri(baseUrl("/accounts/" + identification + "/balance?currency=" + Currency.EUR))
                .retrieve()
                .body(Money.class)
                .getAmount();
        BigDecimal usdAfter = restClient.get()
                .uri(baseUrl("/accounts/" + identification + "/balance?currency=" + Currency.USD))
                .retrieve()
                .body(Money.class)
                .getAmount();

        assertThat(eurAfter).isLessThan(eurBefore);
        assertThat(usdAfter).isGreaterThanOrEqualTo(usdBefore);
    }

    @Test
    void debit_insufficientFundsReturnsError() {
        String identification = "1234765";

        String createJson = "{\"identification\":\"" + identification + "\"}";
        restClient.post()
                .uri(baseUrl("/accounts"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(createJson)
                .retrieve()
                .toBodilessEntity();

        String debitJson = "{\"currency\":\"EUR\",\"amount\":10.00}";

        // Use exchange() with a ConvertibleClientHttpResponse so we can inspect the
        // 400 response body without RestClient throwing an exception.
        ResponseEntity<ErrorResponse> response = restClient.post()
                .uri(baseUrl("/accounts/" + identification + "/debit"))
                .contentType(MediaType.APPLICATION_JSON)
                .body(debitJson)
                .exchange((request, clientResponse) -> {
                    ErrorResponse body = clientResponse.bodyTo(ErrorResponse.class);
                    return ResponseEntity.status(clientResponse.getStatusCode())
                            .headers(clientResponse.getHeaders())
                            .body(body);
                });

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus()).isEqualTo(400);
        assertThat(response.getBody().getError()).isEqualTo("Bad Request");
        assertThat(response.getBody().getMessage()).containsIgnoringCase("Insufficient funds");
    }
}
