package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.VehiclePatchRequestDTO;
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

    public VehicleResponsetDTO patch(UUID id, VehiclePatchRequestDTO dto) {
        Vehicle vehicle = repository.findById(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        if (dto.placa() != null) {
            if (repository.existsByLicensePlateAndIdNot(dto.placa(), id)) {
                throw new LicensePlateAlreadyExistsException(dto.placa());
            }
            vehicle.setLicensePlate(dto.placa());
        }

        if (dto.marca() != null) {
            vehicle.setBrand(dto.marca());
        }

        if (dto.modelo() != null) {
            vehicle.setModel(dto.modelo());
        }

        if (dto.ano() != null) {
            vehicle.setVehicleYear(dto.ano());
        }

        if (dto.cor() != null) {
            vehicle.setColor(dto.cor());
        }

        if (dto.preco() != null) {
            BigDecimal priceInUsd = currencyService.convertBrlToUsd(dto.preco());
            vehicle.setPrice(priceInUsd);
        }

        Vehicle updated = repository.save(vehicle);
        return mapper.toResponseDTO(updated);
    }
}
