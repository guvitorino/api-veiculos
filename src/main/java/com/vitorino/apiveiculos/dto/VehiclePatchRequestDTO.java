package com.vitorino.apiveiculos.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "AtualizacaoParcialVeiculo", description = "Representa a atualização parcial de um veículo")
public record VehiclePatchRequestDTO(

        @Schema(description = "Placa do veículo", example = "ABC1234")
        String placa,

        @Schema(description = "Marca do veículo", example = "Volkswagen")
        String marca,

        @Schema(description = "Modelo do veículo", example = "Fox")
        String modelo,

        @Schema(description = "Ano de fabricação", example = "2008")
        Integer ano,

        @Schema(description = "Cor do veículo", example = "Prata")
        String cor,

        @Schema(description = "Preço do veículo em reais", example = "25000")
        BigDecimal preco
) {}