package com.vitorino.apiveiculos.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExchangeRateServiceIntegrationTest {

    private final MockWebServer awesomeServer = new MockWebServer();
    private final MockWebServer frankfurterServer = new MockWebServer();

    @AfterEach
    void tearDown() throws IOException {
        awesomeServer.shutdown();
        frankfurterServer.shutdown();
    }

    @Test
    @DisplayName("Deve consumir AwesomeAPI com WebClient real")
    void shouldConsumeAwesomeApiWithRealWebClient() throws Exception {
        awesomeServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {"USDBRL":{"bid":"5.42"}}
                        """));

        ExchangeRateService service = service();

        BigDecimal result = service.getDollarRate();

        assertThat(result).isEqualByComparingTo("5.42");
        assertThat(awesomeServer.takeRequest().getPath()).isEqualTo("/awesome");
        assertThat(frankfurterServer.getRequestCount()).isZero();
    }

    @Test
    @DisplayName("Deve usar fallback Frankfurter quando AwesomeAPI falhar")
    void shouldUseFrankfurterFallbackWithRealWebClient() throws Exception {
        awesomeServer.enqueue(new MockResponse().setResponseCode(500));
        frankfurterServer.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("""
                        {"rates":{"BRL":5.50}}
                        """));

        ExchangeRateService service = service();

        BigDecimal result = service.getDollarRate();

        assertThat(result).isEqualByComparingTo("5.50");
        assertThat(awesomeServer.takeRequest().getPath()).isEqualTo("/awesome");
        assertThat(frankfurterServer.takeRequest().getPath()).isEqualTo("/frankfurter");
    }

    @Test
    @DisplayName("Deve lançar excecao quando ambas APIs falharem")
    void shouldThrowExceptionWhenBothApisFail() {
        awesomeServer.enqueue(new MockResponse().setResponseCode(500));
        frankfurterServer.enqueue(new MockResponse().setResponseCode(500));

        ExchangeRateService service = service();

        assertThatThrownBy(service::getDollarRate)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Erro ao buscar cotação do dólar");
    }

    private ExchangeRateService service() {
        return new ExchangeRateService(
                WebClient.builder().build(),
                awesomeServer.url("/awesome").toString(),
                frankfurterServer.url("/frankfurter").toString()
        );
    }
}
