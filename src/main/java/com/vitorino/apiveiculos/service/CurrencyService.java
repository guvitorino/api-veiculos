package com.vitorino.apiveiculos.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

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