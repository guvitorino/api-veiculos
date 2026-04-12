package com.vitorino.apiveiculos.exception;

public class LicensePlateAlreadyExistsException extends RuntimeException {
    public LicensePlateAlreadyExistsException(String licensePlate) {
        super("Placa já cadastrada: " + licensePlate);
    }
}
