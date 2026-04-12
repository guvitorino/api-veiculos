package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.AwesomeApiResponseDTO;
import com.vitorino.apiveiculos.dto.FrankfurterApiResponseDTO;
import com.vitorino.apiveiculos.exception.ExternalServiceException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class ExchangeRateService {
    private final WebClient webClient;

    public ExchangeRateService(WebClient currencyWebClient) {
        this.webClient = currencyWebClient;
    }

    private BigDecimal getDollarRateFromAwesomeApi() {
        String AWESOME_URL = "https://economia.awesomeapi.com.br/json/last/USD-BRL";
        AwesomeApiResponseDTO response = webClient.get()
                .uri(AWESOME_URL)
                .retrieve()
                .bodyToMono(AwesomeApiResponseDTO.class)
                .timeout(Duration.ofSeconds(5))
                .block();

        if (response == null || response.usdb() == null || response.usdb().bid() == null) {
            throw new ExternalServiceException("Resposta inválida da AwesomeAPI.");
        }

        return new BigDecimal(response.usdb().bid());
    }

    private BigDecimal getDollarRateFromFrankfurter() {
        String FRANKFURTER_URL = "https://api.frankfurter.dev/v1/latest?from=USD&to=BRL";
        FrankfurterApiResponseDTO response = webClient.get()
                .uri(FRANKFURTER_URL)
                .retrieve()
                .bodyToMono(FrankfurterApiResponseDTO.class)
                .timeout(Duration.ofSeconds(5))
                .block();

        if (response == null || response.rates() == null || response.rates().BRL() == null) {
            throw new ExternalServiceException("Resposta inválida da Frankfurter.");
        }

        return response.rates().BRL();
    }

    private BigDecimal getValidatedAwesomeRate() {
        BigDecimal rate = getDollarRateFromAwesomeApi();
        validateRate(rate);
        return rate;
    }

    private BigDecimal getValidatedFallbackRate(Exception originalException) {
        try {
            BigDecimal rate = getDollarRateFromFrankfurter();
            validateRate(rate);
            return rate;
        } catch (Exception fallbackEx) {
            throw new ExternalServiceException(
                    "Erro ao buscar cotação do dólar na AwesomeAPI e no fallback Frankfurter.",
                    fallbackEx
            );
        }
    }

    private void validateRate(BigDecimal rate) {
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ExternalServiceException("Cotação do dólar inválida.");
        }
    }

    @Cacheable("dollarRate")
    public BigDecimal getDollarRate() {
        try {
            return getValidatedAwesomeRate();
        } catch (Exception awesomeEx) {
            return getValidatedFallbackRate(awesomeEx);
        }
    }
}
