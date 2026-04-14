package com.vitorino.apiveiculos.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Schema(name = "Veiculo Criado", description = "Representa um veículo criado na API")
public record VehicleResponsetDTO(
        @Schema(description = "id do veículo criado", example = "b3200355-72b2-4447-8214-06116b856a89")
        UUID id,

        @Schema(description = "Placa do veículo", example = "ABC1234")
        String placa,

        @Schema(description = "Marca do veículo", example = "Volkswagem")
        String marca,

        @Schema(description = "Modelo do veículo", example = "Fox")
        String modelo,

        @Schema(description = "Ano de fabricação", example = "2008")
        Integer ano,

        @Schema(description = "Cor do veículo", example = "Prata")
        String cor,

        @Schema(description = "Preço do veículo em USD", example = "4990,82")
        BigDecimal preco
) implements Serializable {}
