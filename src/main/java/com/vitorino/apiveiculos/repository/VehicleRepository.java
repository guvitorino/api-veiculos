package com.vitorino.apiveiculos.repository;

import com.vitorino.apiveiculos.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {
    boolean existsByLicensePlate(String licensePlate);
    boolean existsByLicensePlateAndIdNot(String licensePlate, UUID id);
    Optional<Vehicle> findByIdAndDeletedFalse(UUID id);
}
