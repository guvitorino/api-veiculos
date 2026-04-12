package com.vitorino.apiveiculos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AwesomeApiResponseDTO(
        @JsonProperty("USDBRL") UsdBrl usdb
) {
    public record UsdBrl(
            String bid
    ) {}
}