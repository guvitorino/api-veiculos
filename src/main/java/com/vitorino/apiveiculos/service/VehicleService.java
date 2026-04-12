package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.VehicleRequestDTO;
import com.vitorino.apiveiculos.dto.VehicleResponsetDTO;
import com.vitorino.apiveiculos.exception.LicensePlateAlreadyExistsException;
import com.vitorino.apiveiculos.exception.VehicleNotFoundException;
import com.vitorino.apiveiculos.mapper.VehicleMapper;
import com.vitorino.apiveiculos.model.Vehicle;
import com.vitorino.apiveiculos.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class VehicleService {
    private final VehicleRepository repository;
    private final VehicleMapper mapper;
    private final CurrencyService currencyService;

    public VehicleService(VehicleRepository repository, VehicleMapper mapper, CurrencyService currencyService) {
        this.repository = repository;
        this.mapper = mapper;
        this.currencyService = currencyService;
    }

    public VehicleResponsetDTO save(VehicleRequestDTO dto) {
        Vehicle entity = mapper.toEntity(dto);

        if (repository.existsByLicensePlate(entity.getLicensePlate())) {
            throw new LicensePlateAlreadyExistsException(dto.placa());
        }
        BigDecimal priceInUsd = currencyService.convertBrlToUsd(dto.preco());
        entity.setPrice(priceInUsd);
        Vehicle saved = repository.save(entity);
        return mapper.toResponseDTO(saved);
    }

    public VehicleResponsetDTO update(UUID id, VehicleRequestDTO dto) {
        Vehicle vehicle = repository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        if (repository.existsByLicensePlateAndIdNot(dto.placa(), id)) {
            throw new LicensePlateAlreadyExistsException(dto.placa());
        }

        vehicle.setLicensePlate(dto.placa());
        vehicle.setBrand(dto.marca());
        vehicle.setModel(dto.modelo());
        vehicle.setVehicleYear(dto.ano());
        vehicle.setColor(dto.cor());

        BigDecimal priceInUsd = currencyService.convertBrlToUsd(dto.preco());
        vehicle.setPrice(priceInUsd);

        Vehicle updated = repository.save(vehicle);
        return mapper.toResponseDTO(updated);
    }
}
