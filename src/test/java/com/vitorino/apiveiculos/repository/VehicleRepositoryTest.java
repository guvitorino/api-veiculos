package com.vitorino.apiveiculos.repository;

import com.vitorino.apiveiculos.dto.VehicleByBrandReportDTO;
import com.vitorino.apiveiculos.model.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository repository;

    @Nested
    @DisplayName("existsByLicensePlateAndDeletedFalse()")
    class ExistsByLicensePlate {
        @Test
        @DisplayName("Deve retornar true quando a placa já existe")
        void shouldReturnTrueWhenLicensePlateExists() {
            Vehicle vehicle = new Vehicle();
            vehicle.setLicensePlate("ABC1234");
            vehicle.setBrand("Volkswagen");
            vehicle.setModel("Fox");
            vehicle.setVehicleYear(2008);
            vehicle.setColor("Prata");
            vehicle.setPrice(new BigDecimal("5000.00"));

            repository.save(vehicle);

            boolean exists = repository.existsByLicensePlateAndDeletedFalse("ABC1234");

            assertTrue(exists);
        }

        @Test
        @DisplayName("Deve retornar false quando a placa não existe")
        void shouldReturnFalseWhenLicensePlateDoesNotExist() {
            boolean exists = repository.existsByLicensePlateAndDeletedFalse("XYZ9999");

            assertFalse(exists);
        }

        @Test
        @DisplayName("Não deve considerar placas diferentes")
        void shouldReturnFalseForDifferentLicensePlate() {
            Vehicle vehicle = new Vehicle();
            vehicle.setLicensePlate("ABC1234");
            vehicle.setBrand("Volkswagen");
            vehicle.setModel("Fox");
            vehicle.setVehicleYear(2008);
            vehicle.setColor("Prata");
            vehicle.setPrice(new BigDecimal("5000.00"));

            repository.save(vehicle);

            boolean exists = repository.existsByLicensePlateAndDeletedFalse("DIFFERENT");

            assertFalse(exists);
        }

        @Test
        @DisplayName("Deve ignorar placa de veiculo deletado")
        void shouldIgnoreDeletedVehicleLicensePlate() {
            Vehicle vehicle = new Vehicle();
            vehicle.setLicensePlate("ABC1234");
            vehicle.setBrand("Volkswagen");
            vehicle.setModel("Fox");
            vehicle.setVehicleYear(2008);
            vehicle.setColor("Prata");
            vehicle.setPrice(new BigDecimal("5000.00"));
            vehicle.setDeleted(true);

            repository.save(vehicle);

            boolean exists = repository.existsByLicensePlateAndDeletedFalse("ABC1234");

            assertFalse(exists);
        }
    }

    @Nested
    @DisplayName("ExistsByLicensePlateAndIdNotAndDeletedFalse()")
    class ExistsByLicensePlateAndIdNot {
        @Test
        @DisplayName("Deve retornar true quando a placa existe em outro veículo")
        void shouldReturnTrueWhenLicensePlateExistsInAnotherVehicle() {
            Vehicle vehicle = new Vehicle();
            vehicle.setLicensePlate("ABC1234");
            vehicle.setBrand("Volkswagen");
            vehicle.setModel("Fox");
            vehicle.setVehicleYear(2008);
            vehicle.setColor("Prata");
            vehicle.setPrice(new BigDecimal("5000.00"));

            Vehicle saved = repository.save(vehicle);

            UUID otherId = UUID.randomUUID();

            boolean exists = repository.existsByLicensePlateAndIdNotAndDeletedFalse("ABC1234", otherId);

            assertTrue(exists);
            assertNotEquals(saved.getId(), otherId);
        }

        @Test
        @DisplayName("Deve retornar false quando a placa pertence ao mesmo veículo")
        void shouldReturnFalseWhenLicensePlateBelongsToSameVehicle() {
            Vehicle vehicle = new Vehicle();
            vehicle.setLicensePlate("ABC1234");
            vehicle.setBrand("Volkswagen");
            vehicle.setModel("Fox");
            vehicle.setVehicleYear(2008);
            vehicle.setColor("Prata");
            vehicle.setPrice(new BigDecimal("5000.00"));

            Vehicle saved = repository.save(vehicle);

            boolean exists = repository.existsByLicensePlateAndIdNotAndDeletedFalse("ABC1234", saved.getId());

            assertFalse(exists);
        }

        @Test
        @DisplayName("Deve retornar false quando a placa não existe")
        void shouldReturnFalseWhenLicensePlateDoesNotExistForDifferentId() {
            boolean exists = repository.existsByLicensePlateAndIdNotAndDeletedFalse("ZZZ9999", UUID.randomUUID());

            assertFalse(exists);
        }

        @Test
        @DisplayName("Deve ignorar placa de outro veiculo deletado")
        void shouldIgnoreDeletedVehicleForDifferentIdCheck() {
            Vehicle vehicle = new Vehicle();
            vehicle.setLicensePlate("ABC1234");
            vehicle.setBrand("Volkswagen");
            vehicle.setModel("Fox");
            vehicle.setVehicleYear(2008);
            vehicle.setColor("Prata");
            vehicle.setPrice(new BigDecimal("5000.00"));
            vehicle.setDeleted(true);

            Vehicle saved = repository.save(vehicle);
            UUID otherId = UUID.randomUUID();

            boolean exists = repository.existsByLicensePlateAndIdNotAndDeletedFalse("ABC1234", otherId);

            assertFalse(exists);
            assertNotEquals(saved.getId(), otherId);
        }
    }

    @Nested
    @DisplayName("findByIdAndDeletedFalse()")
    class FindByIdAndDeletedFalse {

        @Test
        @DisplayName("Deve retornar o veículo quando não estiver deletado")
        void shouldReturnVehicleWhenNotDeleted() {
            Vehicle vehicle = new Vehicle();
            vehicle.setLicensePlate("ABC1234");
            vehicle.setBrand("Toyota");
            vehicle.setModel("Corolla");
            vehicle.setVehicleYear(2020);
            vehicle.setColor("Branco");
            vehicle.setPrice(new BigDecimal("80000.00"));
            vehicle.setDeleted(false);

            Vehicle saved = repository.save(vehicle);

            var result = repository.findByIdAndDeletedFalse(saved.getId());

            assertTrue(result.isPresent());
            assertEquals(saved.getId(), result.get().getId());
            assertFalse(result.get().getDeleted());
        }

        @Test
        @DisplayName("Deve retornar vazio quando o veículo estiver deletado")
        void shouldReturnEmptyWhenVehicleIsDeleted() {
            Vehicle vehicle = new Vehicle();
            vehicle.setLicensePlate("DEF5678");
            vehicle.setBrand("Honda");
            vehicle.setModel("Civic");
            vehicle.setVehicleYear(2019);
            vehicle.setColor("Preto");
            vehicle.setPrice(new BigDecimal("75000.00"));
            vehicle.setDeleted(true);

            Vehicle saved = repository.save(vehicle);

            var result = repository.findByIdAndDeletedFalse(saved.getId());

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("Deve retornar vazio quando o veículo não existe")
        void shouldReturnEmptyWhenVehicleDoesNotExist() {
            UUID randomId = UUID.randomUUID();

            var result = repository.findByIdAndDeletedFalse(randomId);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("countVehiclesGroupedByBrand()")
    class CountVehiclesGroupedByBrand {

        @Test
        @DisplayName("Deve retornar quantidade de veículos agrupados por marca")
        void shouldReturnVehicleCountGroupedByBrand() {
            Vehicle vehicle1 = new Vehicle();
            vehicle1.setLicensePlate("ABC1234");
            vehicle1.setBrand("volkswagen");
            vehicle1.setModel("fox");
            vehicle1.setVehicleYear(2008);
            vehicle1.setColor("prata");
            vehicle1.setPrice(new BigDecimal("5000.00"));
            vehicle1.setDeleted(false);

            Vehicle vehicle2 = new Vehicle();
            vehicle2.setLicensePlate("ABC1235");
            vehicle2.setBrand("volkswagen");
            vehicle2.setModel("gol");
            vehicle2.setVehicleYear(2010);
            vehicle2.setColor("preto");
            vehicle2.setPrice(new BigDecimal("6000.00"));
            vehicle2.setDeleted(false);

            Vehicle vehicle3 = new Vehicle();
            vehicle3.setLicensePlate("XYZ9999");
            vehicle3.setBrand("toyota");
            vehicle3.setModel("corolla");
            vehicle3.setVehicleYear(2020);
            vehicle3.setColor("branco");
            vehicle3.setPrice(new BigDecimal("20000.00"));
            vehicle3.setDeleted(false);

            repository.saveAll(List.of(vehicle1, vehicle2, vehicle3));

            List<VehicleByBrandReportDTO> result = repository.countVehiclesGroupedByBrand();

            assertEquals(2, result.size());
            assertEquals("volkswagen", result.get(0).marca());
            assertEquals(2L, result.get(0).quantidade());
            assertEquals("toyota", result.get(1).marca());
            assertEquals(1L, result.get(1).quantidade());
        }
    }
}
