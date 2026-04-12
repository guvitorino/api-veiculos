package com.vitorino.apiveiculos.mapper;

import com.vitorino.apiveiculos.dto.VehicleRequestDTO;
import com.vitorino.apiveiculos.dto.VehicleResponsetDTO;
import com.vitorino.apiveiculos.model.Vehicle;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VehicleMapper {
    @Mapping(source = "placa", target = "licensePlate")
    @Mapping(source = "marca", target = "brand")
    @Mapping(source = "modelo", target = "model")
    @Mapping(source = "ano", target = "vehicleYear")
    @Mapping(source = "cor", target = "color")
    @Mapping(source = "preco", target = "price")
    Vehicle toEntity(VehicleRequestDTO dto);

    @Mapping(source = "licensePlate", target = "placa")
    @Mapping(source = "brand", target = "marca")
    @Mapping(source = "model", target = "modelo")
    @Mapping(source = "vehicleYear", target = "ano")
    @Mapping(source = "color", target = "cor")
    @Mapping(source = "price", target = "preco")
    VehicleResponsetDTO toResponseDTO(Vehicle entity);
}

