package com.vitorino.apiveiculos.dto;

import java.io.Serializable;

public record VehicleByBrandReportDTO(
        String marca,
        Long quantidade
) implements Serializable {
}
