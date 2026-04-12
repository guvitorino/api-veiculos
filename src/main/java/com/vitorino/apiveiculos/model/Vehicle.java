package com.vitorino.apiveiculos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Vehicle {
    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank(message = "Placa é obrigatório")
    @Column(name = "license_plate", unique = true)
    private String licensePlate;

    @NotBlank(message = "Marca é obrigatório")
    private String brand;

    @NotBlank(message = "Modelo é obrigatório")
    private String model;

    @NotNull(message = "Ano é obrigatório")
    private Integer vehicleYear;

    @NotBlank(message = "Cor é obrigatório")
    private String color;

    @NotNull(message = "Preco é obrigatório")
    private BigDecimal price;

    private Boolean deleted = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
