package com.vitorino.apiveiculos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(name = "Veiculo", description = "Representa um veículo na API")
public record VehicleRequestDTO(
        @NotBlank(message = "Placa é obrigatória")
        @Schema(description = "Placa do veículo", example = "ABC1234")
        String placa,

        @Schema(description = "Marca do veículo", example = "Volkswagem")
        @NotBlank(message = "Marca é obrigatória")
        String marca,

        @NotBlank(message = "Modelo é obrigatória")
        @Schema(description = "Modelo do veículo", example = "Fox")
        String modelo,

        @NotNull(message = "Ano é obrigatório")
        @Schema(description = "Ano de fabricação", example = "2008")
        Integer ano,

        @NotBlank(message = "Cor é obrigatória")
        @Schema(description = "Cor do veículo", example = "Prata")
        String cor,

        @NotNull(message = "Preço é obrigatório")
        @Schema(description = "Preço do veículo", example = "25000")
        BigDecimal preco
) {}