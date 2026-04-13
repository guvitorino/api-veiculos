package com.vitorino.apiveiculos.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

public class VehicleFilterDTO {

    @Getter
    @Setter
    private String marca;

    @Getter
    @Setter
    private Integer ano;

    @Getter
    @Setter
    private String cor;

    @Getter
    @Setter
    private BigDecimal minPreco;

    @Getter
    @Setter
    private BigDecimal maxPreco;

}