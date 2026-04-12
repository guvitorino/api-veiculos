package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.VehicleRequestDTO;
import com.vitorino.apiveiculos.dto.VehicleResponsetDTO;
import com.vitorino.apiveiculos.exception.LicensePlateAlreadyExistsException;
import com.vitorino.apiveiculos.mapper.VehicleMapper;
import com.vitorino.apiveiculos.model.Vehicle;
import com.vitorino.apiveiculos.repository.VehicleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

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
}
