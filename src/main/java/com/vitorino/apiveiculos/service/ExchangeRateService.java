package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.AwesomeApiResponseDTO;
import com.vitorino.apiveiculos.dto.FrankfurterApiResponseDTO;
import com.vitorino.apiveiculos.exception.ExternalServiceException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class ExchangeRateService {
    private final WebClient webClient;
    private final String awesomeUrl;
    private final String frankfurterUrl;

    public ExchangeRateService(
            WebClient currencyWebClient,
            @Value("${external.exchange.awesome-url:https://economia.awesomeapi.com.br/json/last/USD-BRL}") String awesomeUrl,
            @Value("${external.exchange.frankfurter-url:https://api.frankfurter.dev/v1/latest?from=USD&to=BRL}") String frankfurterUrl
    ) {
        this.webClient = currencyWebClient;
        this.awesomeUrl = awesomeUrl;
        this.frankfurterUrl = frankfurterUrl;
    }

    private BigDecimal getDollarRateFromAwesomeApi() {
        AwesomeApiResponseDTO response = webClient.get()
                .uri(awesomeUrl)
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
        FrankfurterApiResponseDTO response = webClient.get()
                .uri(frankfurterUrl)
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
