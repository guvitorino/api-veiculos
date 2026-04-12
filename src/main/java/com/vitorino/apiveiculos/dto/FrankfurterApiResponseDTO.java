package com.vitorino.apiveiculos.dto;

import java.math.BigDecimal;

public record FrankfurterApiResponseDTO(
        Rates rates
) {
    public record Rates(
            BigDecimal BRL
    ) {}
}