package com.vitorino.apiveiculos.repository;

import com.vitorino.apiveiculos.dto.VehicleByBrandReportDTO;
import com.vitorino.apiveiculos.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleRepository extends JpaRepository<Vehicle, UUID>, JpaSpecificationExecutor<Vehicle> {
    boolean existsByLicensePlateAndDeletedFalse(String licensePlate);
    boolean existsByLicensePlateAndIdNotAndDeletedFalse(String licensePlate, UUID id);
    Optional<Vehicle> findByIdAndDeletedFalse(UUID id);

    @Query("""
           SELECT new com.vitorino.apiveiculos.dto.VehicleByBrandReportDTO(v.brand, COUNT(v))
           FROM Vehicle v
           WHERE v.deleted = false
           GROUP BY v.brand
           ORDER BY COUNT(v) DESC
           """)
    List<VehicleByBrandReportDTO> countVehiclesGroupedByBrand();
}
