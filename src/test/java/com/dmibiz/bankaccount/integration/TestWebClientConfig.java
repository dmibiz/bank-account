package com.dmibiz.bankaccount.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@TestConfiguration
public class TestWebClientConfig {

    @Bean(name = "testWebClient")
    @Primary
    public WebClient webClient() {
        return WebClient.builder()
                .exchangeFunction(clientRequest -> Mono.just(ClientResponse.create(HttpStatus.OK).build()))
                .build();
    }
}
