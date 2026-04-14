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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    private static final String AWESOME_URL = "https://economia.awesomeapi.com.br/json/last/USD-BRL";
    private static final String FRANKFURTER_URL = "https://api.frankfurter.dev/v1/latest?from=USD&to=BRL";

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
                AWESOME_URL,
                FRANKFURTER_URL
        );
    }

    @Test
    void shouldReturnDollarRateFromAwesomeApi() {
        AwesomeApiResponseDTO awesomeResponse =
                new AwesomeApiResponseDTO(new AwesomeApiResponseDTO.UsdBrl("5.42"));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(AWESOME_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.just(awesomeResponse));

        BigDecimal result = exchangeRateService.getDollarRate();

        assertEquals(new BigDecimal("5.42"), result);

        verify(webClient).get();
        verify(requestHeadersUriSpec).uri(AWESOME_URL);
        verify(requestHeadersSpec).retrieve();
        verify(responseSpec).bodyToMono(AwesomeApiResponseDTO.class);

        verify(requestHeadersUriSpec, never()).uri(FRANKFURTER_URL);
    }

    @Test
    void shouldUseFallbackWhenAwesomeApiFails() {
        FrankfurterApiResponseDTO fallbackResponse =
                new FrankfurterApiResponseDTO(
                        new FrankfurterApiResponseDTO.Rates(new BigDecimal("5.50"))
                );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(AWESOME_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Erro na AwesomeAPI")));

        when(requestHeadersUriSpec.uri(FRANKFURTER_URL))
                .thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToMono(FrankfurterApiResponseDTO.class))
                .thenReturn(Mono.just(fallbackResponse));

        BigDecimal result = exchangeRateService.getDollarRate();

        assertEquals(new BigDecimal("5.50"), result);

        verify(requestHeadersUriSpec).uri(AWESOME_URL);
        verify(requestHeadersUriSpec).uri(FRANKFURTER_URL);
    }

    @Test
    void shouldThrowExceptionWhenAwesomeApiAndFallbackFail() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(AWESOME_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Erro na AwesomeAPI")));

        when(requestHeadersUriSpec.uri(FRANKFURTER_URL))
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
        when(requestHeadersUriSpec.uri(AWESOME_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.just(awesomeResponse));

        when(requestHeadersUriSpec.uri(FRANKFURTER_URL))
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

        when(requestHeadersUriSpec.uri(AWESOME_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Erro na AwesomeAPI")));

        FrankfurterApiResponseDTO fallbackResponse =
                new FrankfurterApiResponseDTO(
                        new FrankfurterApiResponseDTO.Rates(BigDecimal.ZERO)
                );

        when(requestHeadersUriSpec.uri(FRANKFURTER_URL))
                .thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToMono(FrankfurterApiResponseDTO.class))
                .thenReturn(Mono.just(fallbackResponse));

        ExternalServiceException exception = assertThrows(
                ExternalServiceException.class,
                () -> exchangeRateService.getDollarRate()
        );

        assertTrue(exception.getMessage().contains("Erro ao buscar cotação do dólar"));
    }

    @Test
    void shouldUseFallbackWhenAwesomeApiReturnsNullResponse() {
        FrankfurterApiResponseDTO fallbackResponse =
                new FrankfurterApiResponseDTO(
                        new FrankfurterApiResponseDTO.Rates(new BigDecimal("5.50"))
                );

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(AWESOME_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.justOrEmpty(null));

        when(requestHeadersUriSpec.uri(FRANKFURTER_URL))
                .thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToMono(FrankfurterApiResponseDTO.class))
                .thenReturn(Mono.just(fallbackResponse));

        BigDecimal result = exchangeRateService.getDollarRate();

        assertEquals(new BigDecimal("5.50"), result);
    }

    @Test
    void shouldThrowExceptionWhenAwesomeApiReturnsResponseWithoutBidAndFallbackFails() {
        AwesomeApiResponseDTO awesomeResponse = new AwesomeApiResponseDTO(null);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(AWESOME_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.just(awesomeResponse));

        when(requestHeadersUriSpec.uri(FRANKFURTER_URL))
                .thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToMono(FrankfurterApiResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Erro na Frankfurter")));

        assertThatThrownBy(() -> exchangeRateService.getDollarRate())
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Erro ao buscar cotação do dólar");
    }

    @Test
    void shouldThrowExceptionWhenFallbackReturnsResponseWithoutRates() {
        when(webClient.get()).thenReturn(requestHeadersUriSpec);

        when(requestHeadersUriSpec.uri(AWESOME_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.error(new RuntimeException("Erro na AwesomeAPI")));

        FrankfurterApiResponseDTO fallbackResponse = new FrankfurterApiResponseDTO(null);

        when(requestHeadersUriSpec.uri(FRANKFURTER_URL))
                .thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToMono(FrankfurterApiResponseDTO.class))
                .thenReturn(Mono.just(fallbackResponse));

        assertThatThrownBy(() -> exchangeRateService.getDollarRate())
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Erro ao buscar cotação do dólar");
    }

    @Test
    void shouldThrowExceptionWhenAwesomeApiReturnsResponseWithoutBidAndFallbackReturnsResponseWithoutBrl() {
        AwesomeApiResponseDTO awesomeResponse =
                new AwesomeApiResponseDTO(new AwesomeApiResponseDTO.UsdBrl(null));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(AWESOME_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.just(awesomeResponse));

        FrankfurterApiResponseDTO fallbackResponse =
                new FrankfurterApiResponseDTO(new FrankfurterApiResponseDTO.Rates(null));

        when(requestHeadersUriSpec.uri(FRANKFURTER_URL))
                .thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToMono(FrankfurterApiResponseDTO.class))
                .thenReturn(Mono.just(fallbackResponse));

        assertThatThrownBy(() -> exchangeRateService.getDollarRate())
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Erro ao buscar cotação do dólar");
    }

    @Test
    void shouldThrowExceptionWhenAwesomeApiReturnsNegativeRateAndFallbackReturnsNegativeRate() {
        AwesomeApiResponseDTO awesomeResponse =
                new AwesomeApiResponseDTO(new AwesomeApiResponseDTO.UsdBrl("-1.00"));

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(AWESOME_URL))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(AwesomeApiResponseDTO.class))
                .thenReturn(Mono.just(awesomeResponse));

        FrankfurterApiResponseDTO fallbackResponse =
                new FrankfurterApiResponseDTO(new FrankfurterApiResponseDTO.Rates(new BigDecimal("-2.00")));

        when(requestHeadersUriSpec.uri(FRANKFURTER_URL))
                .thenReturn(requestHeadersSpec);
        when(responseSpec.bodyToMono(FrankfurterApiResponseDTO.class))
                .thenReturn(Mono.just(fallbackResponse));

        assertThatThrownBy(() -> exchangeRateService.getDollarRate())
                .isInstanceOf(ExternalServiceException.class)
                .hasMessageContaining("Erro ao buscar cotação do dólar");
    }
}
