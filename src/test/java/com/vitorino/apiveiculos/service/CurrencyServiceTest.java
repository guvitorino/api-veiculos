package com.vitorino.apiveiculos.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private ExchangeRateService exchangeRateService;

    @InjectMocks
    private CurrencyService currencyService;

    @Test
    void shouldConvertBrlToUsdSuccessfully() {
        BigDecimal valueInBrl = new BigDecimal("25000.00");
        BigDecimal dollarRate = new BigDecimal("5.00");

        when(exchangeRateService.getDollarRate()).thenReturn(dollarRate);

        BigDecimal result = currencyService.convertBrlToUsd(valueInBrl);

        assertEquals(new BigDecimal("5000.00"), result);
        verify(exchangeRateService).getDollarRate();
    }

    @Test
    void shouldRoundResultToTwoDecimalPlacesUsingHalfUp() {
        BigDecimal valueInBrl = new BigDecimal("100.00");
        BigDecimal dollarRate = new BigDecimal("3.00");

        when(exchangeRateService.getDollarRate()).thenReturn(dollarRate);

        BigDecimal result = currencyService.convertBrlToUsd(valueInBrl);

        assertEquals(new BigDecimal("33.33"), result);
        verify(exchangeRateService).getDollarRate();
    }

    @Test
    void shouldThrowExceptionWhenValueInBrlIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> currencyService.convertBrlToUsd(null)
        );

        assertEquals("O valor em real não pode ser nulo.", exception.getMessage());
        verify(exchangeRateService, never()).getDollarRate();
    }

    @Test
    void shouldUseExchangeRateServiceToGetDollarRate() {
        BigDecimal valueInBrl = new BigDecimal("50.00");
        BigDecimal dollarRate = new BigDecimal("5.00");

        when(exchangeRateService.getDollarRate()).thenReturn(dollarRate);

        currencyService.convertBrlToUsd(valueInBrl);

        verify(exchangeRateService, times(1)).getDollarRate();
    }
}