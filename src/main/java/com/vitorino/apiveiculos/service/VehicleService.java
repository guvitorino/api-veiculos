package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.*;
import com.vitorino.apiveiculos.exception.InvalidSortFieldException;
import com.vitorino.apiveiculos.exception.LicensePlateAlreadyExistsException;
import com.vitorino.apiveiculos.exception.VehicleNotFoundException;
import com.vitorino.apiveiculos.mapper.VehicleMapper;
import com.vitorino.apiveiculos.model.Vehicle;
import com.vitorino.apiveiculos.repository.VehicleRepository;
import com.vitorino.apiveiculos.specification.VehicleSpecification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
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

    @Transactional
    public void delete(UUID id) {
        Vehicle vehicle = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        vehicle.setDeleted(true);
        repository.save(vehicle);
    }

    public VehicleResponsetDTO findById(UUID id) {
        Vehicle vehicle = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new VehicleNotFoundException(id));

        return mapper.toResponseDTO(vehicle);
    }

    private static final Map<String, String> SORT_MAPPING = Map.of(
            "marca", "brand",
            "ano", "vehicleYear",
            "cor", "color",
            "preco", "price"
    );

    private Pageable mapSortFields(Pageable pageable) {
        List<Sort.Order> orders = pageable.getSort()
                .stream()
                .map(order -> {
                    String mappedProperty = SORT_MAPPING.get(order.getProperty());

                    if (mappedProperty == null) {
                        throw new InvalidSortFieldException(order.getProperty());
                    }

                    return new Sort.Order(order.getDirection(), mappedProperty);
                })
                .toList();

        Sort mappedSort = orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);

        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                mappedSort
        );
    }

    public ListPageResponseDTO<VehicleResponsetDTO> findAll(
            VehicleFilterDTO filters,
            Pageable pageable
    ) {
        Pageable mappedPageable = mapSortFields(pageable);

        Specification<Vehicle> specification = Specification
                .where(VehicleSpecification.notDeleted())
                .and(VehicleSpecification.hasBrand(filters.getMarca()))
                .and(VehicleSpecification.hasYear(filters.getAno()))
                .and(VehicleSpecification.hasColor(filters.getCor()))
                .and(VehicleSpecification.priceGreaterThanOrEqualTo(filters.getMinPreco()))
                .and(VehicleSpecification.priceLessThanOrEqualTo(filters.getMaxPreco()));

        Page<Vehicle> page = repository.findAll(specification, mappedPageable);

        List<VehicleResponsetDTO> data = page.getContent()
                .stream()
                .map(mapper::toResponseDTO)
                .toList();

        return new ListPageResponseDTO<>(
                data,
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
