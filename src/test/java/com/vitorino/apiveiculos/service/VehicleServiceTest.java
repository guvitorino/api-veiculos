package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.VehiclePatchRequestDTO;
import com.vitorino.apiveiculos.dto.VehicleRequestDTO;
import com.vitorino.apiveiculos.dto.VehicleResponsetDTO;
import com.vitorino.apiveiculos.exception.LicensePlateAlreadyExistsException;
import com.vitorino.apiveiculos.exception.VehicleNotFoundException;
import com.vitorino.apiveiculos.mapper.VehicleMapper;
import com.vitorino.apiveiculos.model.Vehicle;
import com.vitorino.apiveiculos.repository.VehicleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
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

    @Nested
    @DisplayName("save()")
    class Save {
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

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        void shouldUpdateVehicleSuccessfully() {
            UUID id = UUID.randomUUID();

            VehicleRequestDTO dto = new VehicleRequestDTO(
                    "ABC1D23",
                    "Toyota",
                    "Corolla",
                    2024,
                    "Preto",
                    new BigDecimal("100000.00")
            );

            Vehicle vehicle = new Vehicle();
            vehicle.setId(id);
            vehicle.setLicensePlate("OLD1234");
            vehicle.setBrand("Honda");
            vehicle.setModel("Civic");
            vehicle.setVehicleYear(2020);
            vehicle.setColor("Branco");
            vehicle.setPrice(new BigDecimal("15000.00"));

            Vehicle savedVehicle = new Vehicle();
            savedVehicle.setId(id);
            savedVehicle.setLicensePlate(dto.placa());
            savedVehicle.setBrand(dto.marca());
            savedVehicle.setModel(dto.modelo());
            savedVehicle.setVehicleYear(dto.ano());
            savedVehicle.setColor(dto.cor());
            savedVehicle.setPrice(new BigDecimal("20000.00"));

            VehicleResponsetDTO responseDTO = new VehicleResponsetDTO(
                    id,
                    dto.placa(),
                    dto.marca(),
                    dto.modelo(),
                    dto.ano(),
                    dto.cor(),
                    new BigDecimal("20000.00")
            );

            when(repository.findById(id)).thenReturn(Optional.of(vehicle));
            when(repository.existsByLicensePlateAndIdNot(dto.placa(), id)).thenReturn(false);
            when(currencyService.convertBrlToUsd(dto.preco())).thenReturn(new BigDecimal("20000.00"));
            when(repository.save(vehicle)).thenReturn(savedVehicle);
            when(mapper.toResponseDTO(savedVehicle)).thenReturn(responseDTO);

            VehicleResponsetDTO result = service.update(id, dto);

            assertNotNull(result);
            assertEquals(responseDTO, result);

            assertEquals(dto.placa(), vehicle.getLicensePlate());
            assertEquals(dto.marca(), vehicle.getBrand());
            assertEquals(dto.modelo(), vehicle.getModel());
            assertEquals(dto.ano(), vehicle.getVehicleYear());
            assertEquals(dto.cor(), vehicle.getColor());
            assertEquals(new BigDecimal("20000.00"), vehicle.getPrice());

            verify(repository).findById(id);
            verify(repository).existsByLicensePlateAndIdNot(dto.placa(), id);
            verify(currencyService).convertBrlToUsd(dto.preco());
            verify(repository).save(vehicle);
            verify(mapper).toResponseDTO(savedVehicle);
        }

        @Test
        void shouldThrowVehicleNotFoundExceptionWhenVehicleDoesNotExist() {
            UUID id = UUID.randomUUID();

            VehicleRequestDTO dto = new VehicleRequestDTO(
                    "ABC1D23",
                    "Toyota",
                    "Corolla",
                    2024,
                    "Preto",
                    new BigDecimal("100000.00")
            );

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThrows(VehicleNotFoundException.class, () -> service.update(id, dto));

            verify(repository).findById(id);
            verify(repository, never()).existsByLicensePlateAndIdNot(anyString(), any(UUID.class));
            verify(currencyService, never()).convertBrlToUsd(any());
            verify(repository, never()).save(any());
            verify(mapper, never()).toResponseDTO(any());
        }

        @Test
        void shouldThrowLicensePlateAlreadyExistsExceptionWhenPlateBelongsToAnotherVehicle() {
            UUID id = UUID.randomUUID();

            VehicleRequestDTO dto = new VehicleRequestDTO(
                    "ABC1D23",
                    "Toyota",
                    "Corolla",
                    2024,
                    "Preto",
                    new BigDecimal("100000.00")
            );

            Vehicle vehicle = new Vehicle();
            vehicle.setId(id);

            when(repository.findById(id)).thenReturn(Optional.of(vehicle));
            when(repository.existsByLicensePlateAndIdNot(dto.placa(), id)).thenReturn(true);

            assertThrows(LicensePlateAlreadyExistsException.class, () -> service.update(id, dto));

            verify(repository).findById(id);
            verify(repository).existsByLicensePlateAndIdNot(dto.placa(), id);
            verify(currencyService, never()).convertBrlToUsd(any());
            verify(repository, never()).save(any());
            verify(mapper, never()).toResponseDTO(any());
        }
    }

    @Nested
    @DisplayName("patch()")
    class PatchTests {

        @Test
        @DisplayName("Deve atualizar apenas os campos informados")
        void shouldUpdateOnlyProvidedFields() {
            UUID id = UUID.randomUUID();

            Vehicle existingVehicle = new Vehicle();
            existingVehicle.setId(id);
            existingVehicle.setLicensePlate("ABC1234");
            existingVehicle.setBrand("Volkswagen");
            existingVehicle.setModel("Fox");
            existingVehicle.setVehicleYear(2008);
            existingVehicle.setColor("Prata");
            existingVehicle.setPrice(new BigDecimal("5000.00"));

            VehiclePatchRequestDTO patchDTO = new VehiclePatchRequestDTO(
                    null,
                    "Fiat",
                    null,
                    null,
                    "Preto",
                    null
            );

            VehicleResponsetDTO responseDTO = new VehicleResponsetDTO(
                    id,
                    "ABC1234",
                    "Fiat",
                    "Fox",
                    2008,
                    "Preto",
                    new BigDecimal("5000.00")
            );

            when(repository.findById(id)).thenReturn(Optional.of(existingVehicle));
            when(repository.save(existingVehicle)).thenReturn(existingVehicle);
            when(mapper.toResponseDTO(existingVehicle)).thenReturn(responseDTO);

            VehicleResponsetDTO result = service.patch(id, patchDTO);

            assertNotNull(result);
            assertEquals("Fiat", existingVehicle.getBrand());
            assertEquals("Preto", existingVehicle.getColor());
            assertEquals("ABC1234", existingVehicle.getLicensePlate());
            assertEquals("Fox", existingVehicle.getModel());
            assertEquals(2008, existingVehicle.getVehicleYear());
            assertEquals(new BigDecimal("5000.00"), existingVehicle.getPrice());

            verify(repository).findById(id);
            verify(repository).save(existingVehicle);
            verify(mapper).toResponseDTO(existingVehicle);
            verify(currencyService, never()).convertBrlToUsd(any());
            verify(repository, never()).existsByLicensePlateAndIdNot(any(), any());
        }

        @Test
        @DisplayName("Deve converter o preço quando preco for informado")
        void shouldConvertPriceWhenPriceIsProvided() {
            UUID id = UUID.randomUUID();

            Vehicle existingVehicle = new Vehicle();
            existingVehicle.setId(id);
            existingVehicle.setLicensePlate("ABC1234");
            existingVehicle.setBrand("Volkswagen");
            existingVehicle.setModel("Fox");
            existingVehicle.setVehicleYear(2008);
            existingVehicle.setColor("Prata");
            existingVehicle.setPrice(new BigDecimal("5000.00"));

            VehiclePatchRequestDTO patchDTO = new VehiclePatchRequestDTO(
                    null,
                    null,
                    null,
                    null,
                    null,
                    new BigDecimal("30000.00")
            );

            when(repository.findById(id)).thenReturn(Optional.of(existingVehicle));
            when(currencyService.convertBrlToUsd(new BigDecimal("30000.00")))
                    .thenReturn(new BigDecimal("6000.00"));
            when(repository.save(existingVehicle)).thenReturn(existingVehicle);
            when(mapper.toResponseDTO(existingVehicle)).thenReturn(
                    new VehicleResponsetDTO(
                            id,
                            "ABC1234",
                            "Volkswagen",
                            "Fox",
                            2008,
                            "Prata",
                            new BigDecimal("6000.00")
                    )
            );

            service.patch(id, patchDTO);

            assertEquals(new BigDecimal("6000.00"), existingVehicle.getPrice());
            verify(currencyService).convertBrlToUsd(new BigDecimal("30000.00"));
            verify(repository).save(existingVehicle);
        }

        @Test
        @DisplayName("Deve validar placa duplicada quando placa for informada")
        void shouldValidateDuplicateLicensePlateWhenPlateIsProvided() {
            UUID id = UUID.randomUUID();

            Vehicle existingVehicle = new Vehicle();
            existingVehicle.setId(id);
            existingVehicle.setLicensePlate("ABC1234");

            VehiclePatchRequestDTO patchDTO = new VehiclePatchRequestDTO(
                    "XYZ9999",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            when(repository.findById(id)).thenReturn(Optional.of(existingVehicle));
            when(repository.existsByLicensePlateAndIdNot("XYZ9999", id)).thenReturn(true);

            assertThrows(LicensePlateAlreadyExistsException.class, () -> service.patch(id, patchDTO));

            verify(repository).findById(id);
            verify(repository).existsByLicensePlateAndIdNot("XYZ9999", id);
            verify(repository, never()).save(any());
            verify(currencyService, never()).convertBrlToUsd(any());
            verify(mapper, never()).toResponseDTO(any());
        }

        @Test
        @DisplayName("Não deve validar placa duplicada quando placa não for informada")
        void shouldNotValidateDuplicateLicensePlateWhenPlateIsNotProvided() {
            UUID id = UUID.randomUUID();

            Vehicle existingVehicle = new Vehicle();
            existingVehicle.setId(id);
            existingVehicle.setLicensePlate("ABC1234");
            existingVehicle.setBrand("Volkswagen");
            existingVehicle.setModel("Fox");
            existingVehicle.setVehicleYear(2008);
            existingVehicle.setColor("Prata");
            existingVehicle.setPrice(new BigDecimal("5000.00"));

            VehiclePatchRequestDTO patchDTO = new VehiclePatchRequestDTO(
                    null,
                    null,
                    "Uno",
                    null,
                    null,
                    null
            );

            when(repository.findById(id)).thenReturn(Optional.of(existingVehicle));
            when(repository.save(existingVehicle)).thenReturn(existingVehicle);
            when(mapper.toResponseDTO(existingVehicle)).thenReturn(
                    new VehicleResponsetDTO(
                            id,
                            "ABC1234",
                            "Volkswagen",
                            "Uno",
                            2008,
                            "Prata",
                            new BigDecimal("5000.00")
                    )
            );

            service.patch(id, patchDTO);

            verify(repository, never()).existsByLicensePlateAndIdNot(any(), any());
            verify(repository).save(existingVehicle);
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não for encontrado")
        void shouldThrowExceptionWhenVehicleIsNotFound() {
            UUID id = UUID.randomUUID();

            VehiclePatchRequestDTO patchDTO = new VehiclePatchRequestDTO(
                    null,
                    "Fiat",
                    null,
                    null,
                    null,
                    null
            );

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThrows(VehicleNotFoundException.class, () -> service.patch(id, patchDTO));

            verify(repository).findById(id);
            verify(repository, never()).save(any());
            verify(repository, never()).existsByLicensePlateAndIdNot(any(), any());
            verify(currencyService, never()).convertBrlToUsd(any());
            verify(mapper, never()).toResponseDTO(any());
        }

        @Test
        @DisplayName("Deve salvar a entidade atualizada")
        void shouldSaveUpdatedEntity() {
            UUID id = UUID.randomUUID();

            Vehicle existingVehicle = new Vehicle();
            existingVehicle.setId(id);
            existingVehicle.setLicensePlate("ABC1234");
            existingVehicle.setBrand("Volkswagen");
            existingVehicle.setModel("Fox");
            existingVehicle.setVehicleYear(2008);
            existingVehicle.setColor("Prata");
            existingVehicle.setPrice(new BigDecimal("5000.00"));

            VehiclePatchRequestDTO patchDTO = new VehiclePatchRequestDTO(
                    "XYZ9999",
                    "Fiat",
                    "Uno",
                    2010,
                    "Preto",
                    new BigDecimal("35000.00")
            );

            when(repository.findById(id)).thenReturn(Optional.of(existingVehicle));
            when(repository.existsByLicensePlateAndIdNot("XYZ9999", id)).thenReturn(false);
            when(currencyService.convertBrlToUsd(new BigDecimal("35000.00")))
                    .thenReturn(new BigDecimal("7000.00"));
            when(repository.save(any(Vehicle.class))).thenReturn(existingVehicle);
            when(mapper.toResponseDTO(existingVehicle)).thenReturn(
                    new VehicleResponsetDTO(
                            id,
                            "XYZ9999",
                            "Fiat",
                            "Uno",
                            2010,
                            "Preto",
                            new BigDecimal("7000.00")
                    )
            );

            service.patch(id, patchDTO);

            ArgumentCaptor<Vehicle> captor = ArgumentCaptor.forClass(Vehicle.class);
            verify(repository).save(captor.capture());

            Vehicle savedVehicle = captor.getValue();

            assertEquals("XYZ9999", savedVehicle.getLicensePlate());
            assertEquals("Fiat", savedVehicle.getBrand());
            assertEquals("Uno", savedVehicle.getModel());
            assertEquals(2010, savedVehicle.getVehicleYear());
            assertEquals("Preto", savedVehicle.getColor());
            assertEquals(new BigDecimal("7000.00"), savedVehicle.getPrice());
        }
    }

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("Deve realizar soft delete com sucesso")
        void shouldSoftDeleteVehicleSuccessfully() {
            UUID id = UUID.randomUUID();

            Vehicle vehicle = new Vehicle();
            vehicle.setId(id);
            vehicle.setLicensePlate("ABC1234");
            vehicle.setBrand("Volkswagen");
            vehicle.setModel("Fox");
            vehicle.setVehicleYear(2008);
            vehicle.setColor("Prata");
            vehicle.setPrice(new BigDecimal("5000.00"));
            vehicle.setDeleted(false);

            when(repository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(vehicle));
            when(repository.save(vehicle)).thenReturn(vehicle);

            service.delete(id);

            assertTrue(vehicle.getDeleted());

            verify(repository).findByIdAndDeletedFalse(id);
            verify(repository).save(vehicle);
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não for encontrado")
        void shouldThrowExceptionWhenVehicleIsNotFound() {
            UUID id = UUID.randomUUID();

            when(repository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

            assertThrows(VehicleNotFoundException.class, () -> service.delete(id));

            verify(repository).findByIdAndDeletedFalse(id);
            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Deve retornar veículo quando encontrado")
        void shouldReturnVehicleWhenFound() {
            UUID id = UUID.randomUUID();

            Vehicle vehicle = new Vehicle();
            vehicle.setId(id);
            vehicle.setLicensePlate("ABC1234");

            VehicleResponsetDTO responseDTO = new VehicleResponsetDTO(
                    id,
                    "ABC1234",
                    "Volkswagen",
                    "Fox",
                    2008,
                    "Prata",
                    new BigDecimal("5000.00")
            );

            when(repository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(vehicle));
            when(mapper.toResponseDTO(vehicle)).thenReturn(responseDTO);

            VehicleResponsetDTO result = service.findById(id);

            assertNotNull(result);
            assertEquals(id, result.id());

            verify(repository).findByIdAndDeletedFalse(id);
            verify(mapper).toResponseDTO(vehicle);
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não for encontrado")
        void shouldThrowExceptionWhenVehicleNotFound() {
            UUID id = UUID.randomUUID();

            when(repository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

            assertThrows(VehicleNotFoundException.class, () -> service.findById(id));

            verify(repository).findByIdAndDeletedFalse(id);
            verify(mapper, never()).toResponseDTO(any());
        }
    }
}