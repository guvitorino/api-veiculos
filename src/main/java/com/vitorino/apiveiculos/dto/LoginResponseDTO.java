package com.vitorino.apiveiculos.dto;

public record LoginResponseDTO(
        String token,
        String type
) {
}