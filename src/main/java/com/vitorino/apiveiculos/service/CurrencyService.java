package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.AwesomeApiResponseDTO;
import com.vitorino.apiveiculos.dto.FrankfurterApiResponseDTO;
import com.vitorino.apiveiculos.exception.ExternalServiceException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;

@Service
public class CurrencyService {

    private final ExchangeRateService exchangeRateService;

    public CurrencyService(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    public BigDecimal convertBrlToUsd(BigDecimal valueInBrl) {
        if (valueInBrl == null) {
            throw new IllegalArgumentException("O valor em real não pode ser nulo.");
        }

        BigDecimal dollarRate = this.exchangeRateService.getDollarRate();

        return valueInBrl.divide(dollarRate, 2, RoundingMode.HALF_UP);
    }


}