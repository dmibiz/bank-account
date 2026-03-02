package com.dmibiz.bankaccount.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ExternalLoggingService {

    private final WebClient webClient;

    public void logDebit() {
        webClient.get()
                .uri("/200")
                .retrieve()
                .onStatus(HttpStatusCode::isError,
                        response -> Mono.error(new RuntimeException("External logging failed")))
                .toBodilessEntity()
                .timeout(Duration.ofSeconds(3))
                .block(); // block because the service is not reactive
    }
}
