package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.AwesomeApiResponseDTO;
import com.vitorino.apiveiculos.dto.FrankfurterApiResponseDTO;
import com.vitorino.apiveiculos.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private WebClient webClient;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @SuppressWarnings("rawtypes")
    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ExchangeRateService exchangeRateService;

    @BeforeEach
    void setUp() {
        exchangeRateService = new ExchangeRateService(
                webClient,
                "https://economia.awesomeapi.com.br/json/last/USD-BRL",
                "https://api.frankfurter.dev/v1/latest?from=USD&to=BRL"
        );
    }

    @Test
    void shouldReturnDollarRateFromAwesomeApi() {
        AwesomeApiResponseDTO awesomeResponse =
                new AwesomeApiResponseDTO(new AwesomeApiResponseDTO.UsdBrl("5.42"));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("https://economia.awesomeapi.com.br/json/last/USD-BRL"))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.just(awesomeResponse));

        BigDecimal result = exchangeRateService.getDollarRate();

        assertEquals(new BigDecimal("5.42"), result);

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri("https://economia.awesomeapi.com.br/json/last/USD-BRL");
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(AwesomeApiResponseDTO.class);

        verify(requestHeadersUriSpec, never()).uri("https://api.frankfurter.dev/v1/latest?from=USD&to=BRL");
    }

    @Test
    void shouldUseFallbackWhenAwesomeApiFails() {
        FrankfurterApiResponseDTO fallbackResponse =
                new FrankfurterApiResponseDTO(
                        new FrankfurterApiResponseDTO.Rates(new BigDecimal("5.50"))
                );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri("https://economia.awesomeapi.com.br/json/last/USD-BRL"))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Erro na AwesomeAPI")));

        when(requestHeadersUriSpec.uri("https://api.frankfurter.dev/v1/latest?from=USD&to=BRL"))
                .thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToMono(FrankfurterApiResponseDTO.class))
                .thenReturn(Mono.just(fallbackResponse));

        BigDecimal result = exchangeRateService.getDollarRate();

        assertEquals(new BigDecimal("5.50"), result);

        verify(requestHeadersUriSpec).uri("https://economia.awesomeapi.com.br/json/last/USD-BRL");
        verify(requestHeadersUriSpec).uri("https://api.frankfurter.dev/v1/latest?from=USD&to=BRL");
    }

    @Test
    void shouldThrowExceptionWhenAwesomeApiAndFallbackFail() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri("https://economia.awesomeapi.com.br/json/last/USD-BRL"))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Erro na AwesomeAPI")));

        when(requestHeadersUriSpec.uri("https://api.frankfurter.dev/v1/latest?from=USD&to=BRL"))
                .thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToMono(FrankfurterApiResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Erro na Frankfurter")));

        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> exchangeRateService.getDollarRate()
        );

        assertTrue(exception.getMessage().contains("Erro ao buscar cotação do dólar"));
    }

    @Test
    void shouldThrowExceptionWhenAwesomeApiReturnsInvalidRate() {
        AwesomeApiResponseDTO awesomeResponse =
                new AwesomeApiResponseDTO(new AwesomeApiResponseDTO.UsdBrl("0"));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri("https://economia.awesomeapi.com.br/json/last/USD-BRL"))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.just(awesomeResponse));

        when(requestHeadersUriSpec.uri("https://api.frankfurter.dev/v1/latest?from=USD&to=BRL"))
                .thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToMono(FrankfurterApiResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Fallback também falhou")));

        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> exchangeRateService.getDollarRate()
        );

        assertTrue(exception.getMessage().contains("Erro ao buscar cotação do dólar"));
    }

    @Test
    void shouldThrowExceptionWhenFallbackReturnsInvalidRate() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri("https://economia.awesomeapi.com.br/json/last/USD-BRL"))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Erro na AwesomeAPI")));

        FrankfurterApiResponseDTO fallbackResponse =
                new FrankfurterApiResponseDTO(
                        new FrankfurterApiResponseDTO.Rates(BigDecimal.ZERO)
                );

        when(requestHeadersUriSpec.uri("https://api.frankfurter.dev/v1/latest?from=USD&to=BRL"))
                .thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToMono(FrankfurterApiResponseDTO.class))
                .thenReturn(Mono.just(fallbackResponse));

        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> exchangeRateService.getDollarRate()
        );

        assertTrue(exception.getMessage().contains("Erro ao buscar cotação do dólar"));
    }
}
