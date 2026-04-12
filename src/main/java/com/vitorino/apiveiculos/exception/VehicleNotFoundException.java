package com.vitorino.apiveiculos.exception;

import java.util.UUID;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(UUID id) {
        super("Veículo não encontrado com id: " + id);
    }
}