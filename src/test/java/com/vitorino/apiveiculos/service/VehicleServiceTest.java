package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.VehicleRequestDTO;
import com.vitorino.apiveiculos.dto.VehicleResponsetDTO;
import com.vitorino.apiveiculos.exception.LicensePlateAlreadyExistsException;
import com.vitorino.apiveiculos.mapper.VehicleMapper;
import com.vitorino.apiveiculos.model.Vehicle;
import com.vitorino.apiveiculos.repository.VehicleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VehicleServiceTest {

    @Mock
    private VehicleRepository repository;

    @Mock
    private VehicleMapper mapper;

    @Mock
    private CurrencyService currencyService;

    @InjectMocks
    private VehicleService service;

    final UUID id = UUID.randomUUID();

    @Test
    void shouldSaveVehicleSuccessfully() {
        VehicleRequestDTO requestDTO = new VehicleRequestDTO(
                "ABC1234",
                "Volkswagen",
                "Fox",
                2008,
                "Prata",
                new BigDecimal("25000.00")
        );

        Vehicle entity = new Vehicle();
        entity.setLicensePlate("ABC1234");
        entity.setBrand("Volkswagen");
        entity.setModel("Fox");
        entity.setVehicleYear(2008);
        entity.setColor("Prata");

        Vehicle savedEntity = new Vehicle();

        savedEntity.setId(id);
        savedEntity.setLicensePlate("ABC1234");
        savedEntity.setBrand("Volkswagen");
        savedEntity.setModel("Fox");
        savedEntity.setVehicleYear(2008);
        savedEntity.setColor("Prata");
        savedEntity.setPrice(new BigDecimal("5000.00"));

        VehicleResponsetDTO responseDTO = new VehicleResponsetDTO(
                id,
                "ABC1234",
                "Volkswagen",
                "Fox",
                2008,
                "Prata",
                new BigDecimal("5000.00")
        );

        when(mapper.toEntity(requestDTO)).thenReturn(entity);
        when(repository.existsByLicensePlate("ABC1234")).thenReturn(false);
        when(currencyService.convertBrlToUsd(new BigDecimal("25000.00")))
                .thenReturn(new BigDecimal("5000.00"));
        when(repository.save(entity)).thenReturn(savedEntity);
        when(mapper.toResponseDTO(savedEntity)).thenReturn(responseDTO);

        VehicleResponsetDTO result = service.save(requestDTO);

        assertNotNull(result);
        assertEquals(id, result.id());
        assertEquals("ABC1234", result.placa());
        assertEquals(new BigDecimal("5000.00"), result.preco());

        verify(mapper).toEntity(requestDTO);
        verify(repository).existsByLicensePlate("ABC1234");
        verify(currencyService).convertBrlToUsd(new BigDecimal("25000.00"));
        verify(repository).save(entity);
        verify(mapper).toResponseDTO(savedEntity);
    }

    @Test
    void shouldSetConvertedPriceBeforeSaving() {
        VehicleRequestDTO requestDTO = new VehicleRequestDTO(
                "ABC1234",
                "Volkswagen",
                "Fox",
                2008,
                "Prata",
                new BigDecimal("25000.00")
        );

        Vehicle entity = new Vehicle();
        entity.setLicensePlate("ABC1234");

        Vehicle savedEntity = new Vehicle();
        savedEntity.setId(id);
        savedEntity.setLicensePlate("ABC1234");
        savedEntity.setPrice(new BigDecimal("5000.00"));

        VehicleResponsetDTO responseDTO = new VehicleResponsetDTO(
                id,
                "ABC1234",
                "Volkswagen",
                "Fox",
                2008,
                "Prata",
                new BigDecimal("5000.00")
        );

        when(mapper.toEntity(requestDTO)).thenReturn(entity);
        when(repository.existsByLicensePlate("ABC1234")).thenReturn(false);
        when(currencyService.convertBrlToUsd(new BigDecimal("25000.00")))
                .thenReturn(new BigDecimal("5000.00"));
        when(repository.save(any(Vehicle.class))).thenReturn(savedEntity);
        when(mapper.toResponseDTO(savedEntity)).thenReturn(responseDTO);

        service.save(requestDTO);

        ArgumentCaptor<Vehicle> vehicleCaptor = ArgumentCaptor.forClass(Vehicle.class);
        verify(repository).save(vehicleCaptor.capture());

        Vehicle vehicleSaved = vehicleCaptor.getValue();
        assertEquals(new BigDecimal("5000.00"), vehicleSaved.getPrice());
    }

    @Test
    void shouldThrowExceptionWhenLicensePlateAlreadyExists() {
        VehicleRequestDTO requestDTO = new VehicleRequestDTO(
                "ABC1234",
                "Volkswagen",
                "Fox",
                2008,
                "Prata",
                new BigDecimal("25000.00")
        );

        Vehicle entity = new Vehicle();
        entity.setLicensePlate("ABC1234");

        when(mapper.toEntity(requestDTO)).thenReturn(entity);
        when(repository.existsByLicensePlate("ABC1234")).thenReturn(true);

        LicensePlateAlreadyExistsException exception = assertThrows(
                LicensePlateAlreadyExistsException.class,
                () -> service.save(requestDTO)
        );

        assertTrue(exception.getMessage().contains("ABC1234"));

        verify(mapper).toEntity(requestDTO);
        verify(repository).existsByLicensePlate("ABC1234");
        verify(currencyService, never()).convertBrlToUsd(any());
        verify(repository, never()).save(any());
        verify(mapper, never()).toResponseDTO(any());
    }
}