package com.vitorino.apiveiculos.repository;

import com.vitorino.apiveiculos.model.Vehicle;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VehicleRepositoryTest {

    @Autowired
    private VehicleRepository repository;

    @Nested
    @DisplayName("existsByLicensePlate()")
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

            boolean exists = repository.existsByLicensePlate("ABC1234");

            assertTrue(exists);
        }

        @Test
        @DisplayName("Deve retornar false quando a placa não existe")
        void shouldReturnFalseWhenLicensePlateDoesNotExist() {
            boolean exists = repository.existsByLicensePlate("XYZ9999");

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

            boolean exists = repository.existsByLicensePlate("DIFFERENT");

            assertFalse(exists);
        }
    }

    @Nested
    @DisplayName("ExistsByLicensePlateAndIdNot()")
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

            boolean exists = repository.existsByLicensePlateAndIdNot("ABC1234", otherId);

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

            boolean exists = repository.existsByLicensePlateAndIdNot("ABC1234", saved.getId());

            assertFalse(exists);
        }

        @Test
        @DisplayName("Deve retornar false quando a placa não existe")
        void shouldReturnFalseWhenLicensePlateDoesNotExistForDifferentId() {
            boolean exists = repository.existsByLicensePlateAndIdNot("ZZZ9999", UUID.randomUUID());

            assertFalse(exists);
        }
    }
}