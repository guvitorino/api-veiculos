package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.ListPageResponseDTO;
import com.vitorino.apiveiculos.dto.VehicleByBrandReportDTO;
import com.vitorino.apiveiculos.dto.VehicleFilterDTO;
import com.vitorino.apiveiculos.dto.VehicleRequestDTO;
import com.vitorino.apiveiculos.dto.VehicleResponsetDTO;
import com.vitorino.apiveiculos.mapper.VehicleMapper;
import com.vitorino.apiveiculos.model.Vehicle;
import com.vitorino.apiveiculos.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(VehicleServiceCacheTest.Config.class)
class VehicleServiceCacheTest {

    @Configuration
    @EnableCaching
    static class Config {
        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("vehicleById", "vehicleList", "vehicleReportByBrand");
        }

        @Bean
        VehicleService vehicleService(VehicleRepository repository, VehicleMapper mapper, CurrencyService currencyService) {
            return new VehicleService(repository, mapper, currencyService);
        }
    }

    @MockitoBean
    private VehicleRepository repository;

    @MockitoBean
    private VehicleMapper mapper;

    @MockitoBean
    private CurrencyService currencyService;

    @jakarta.annotation.Resource
    private VehicleService service;

    @jakarta.annotation.Resource
    private CacheManager cacheManager;

    @BeforeEach
    void setUp() {
        cacheManager.getCacheNames().forEach(name -> cacheManager.getCache(name).clear());
    }

    @Test
    @DisplayName("Deve usar cache no findById")
    void shouldUseCacheOnFindById() {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = vehicle(id, "ABC1234");
        VehicleResponsetDTO response = response(id, "ABC1234");

        when(repository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(vehicle));
        when(mapper.toResponseDTO(vehicle)).thenReturn(response);

        VehicleResponsetDTO first = service.findById(id);
        VehicleResponsetDTO second = service.findById(id);

        assertThat(first).isEqualTo(response);
        assertThat(second).isEqualTo(response);
        verify(repository, times(1)).findByIdAndDeletedFalse(id);
        verify(mapper, times(1)).toResponseDTO(vehicle);
    }

    @Test
    @DisplayName("Deve usar cache no findAll para mesma consulta")
    void shouldUseCacheOnFindAll() {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = vehicle(id, "ABC1234");
        VehicleResponsetDTO response = response(id, "ABC1234");
        VehicleFilterDTO filters = new VehicleFilterDTO();
        filters.setMarca("Volkswagen");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("marca").ascending());
        Page<Vehicle> page = new PageImpl<>(List.of(vehicle), pageable, 1);

        when(repository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(mapper.toResponseDTO(vehicle)).thenReturn(response);

        ListPageResponseDTO<VehicleResponsetDTO> first = service.findAll(filters, pageable);
        ListPageResponseDTO<VehicleResponsetDTO> second = service.findAll(filters, pageable);

        assertThat(first.data()).hasSize(1);
        assertThat(second.data()).hasSize(1);
        verify(repository, times(1)).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
        verify(mapper, times(1)).toResponseDTO(vehicle);
    }

    @Test
    @DisplayName("Deve usar cache no relatorio por marca")
    void shouldUseCacheOnReportByBrand() {
        List<VehicleByBrandReportDTO> report = List.of(new VehicleByBrandReportDTO("Volkswagen", 2L));

        when(repository.countVehiclesGroupedByBrand()).thenReturn(report);

        List<VehicleByBrandReportDTO> first = service.getVehicleReportByBrand();
        List<VehicleByBrandReportDTO> second = service.getVehicleReportByBrand();

        assertThat(first).containsExactlyElementsOf(report);
        assertThat(second).containsExactlyElementsOf(report);
        verify(repository, times(1)).countVehiclesGroupedByBrand();
    }

    @Test
    @DisplayName("Deve invalidar caches de lista e relatorio apos salvar")
    void shouldEvictListAndReportCachesAfterSave() {
        UUID id = UUID.randomUUID();
        Vehicle vehicle = vehicle(id, "ABC1234");
        VehicleResponsetDTO response = response(id, "ABC1234");
        VehicleFilterDTO filters = new VehicleFilterDTO();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("marca").ascending());
        Page<Vehicle> page = new PageImpl<>(List.of(vehicle), pageable, 1);
        VehicleRequestDTO request = new VehicleRequestDTO(
                "XYZ9999",
                "Toyota",
                "Corolla",
                2024,
                "Preto",
                new BigDecimal("100000.00")
        );
        Vehicle newEntity = vehicle(UUID.randomUUID(), "XYZ9999");
        Vehicle savedEntity = vehicle(UUID.randomUUID(), "XYZ9999");
        VehicleResponsetDTO savedResponse = response(savedEntity.getId(), "XYZ9999");

        when(repository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(page);
        when(mapper.toResponseDTO(vehicle)).thenReturn(response);
        when(repository.countVehiclesGroupedByBrand()).thenReturn(List.of(new VehicleByBrandReportDTO("Volkswagen", 1L)));
        when(mapper.toEntity(request)).thenReturn(newEntity);
        when(repository.existsByLicensePlateAndDeletedFalse("XYZ9999")).thenReturn(false);
        when(currencyService.convertBrlToUsd(new BigDecimal("100000.00"))).thenReturn(new BigDecimal("20000.00"));
        when(repository.save(newEntity)).thenReturn(savedEntity);
        when(mapper.toResponseDTO(savedEntity)).thenReturn(savedResponse);

        service.findAll(filters, pageable);
        service.getVehicleReportByBrand();

        service.save(request);

        service.findAll(filters, pageable);
        service.getVehicleReportByBrand();

        verify(repository, times(2)).findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class));
        verify(repository, times(2)).countVehiclesGroupedByBrand();
    }

    private Vehicle vehicle(UUID id, String plate) {
        Vehicle vehicle = new Vehicle();
        vehicle.setId(id);
        vehicle.setLicensePlate(plate);
        vehicle.setBrand("Volkswagen");
        vehicle.setModel("Fox");
        vehicle.setVehicleYear(2020);
        vehicle.setColor("Prata");
        vehicle.setPrice(new BigDecimal("5000.00"));
        vehicle.setDeleted(false);
        return vehicle;
    }

    private VehicleResponsetDTO response(UUID id, String plate) {
        return new VehicleResponsetDTO(
                id,
                plate,
                "Volkswagen",
                "Fox",
                2020,
                "Prata",
                new BigDecimal("5000.00")
        );
    }
}
