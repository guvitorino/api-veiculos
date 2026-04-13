package com.vitorino.apiveiculos.exception;

public class InvalidSortFieldException extends RuntimeException {

    public InvalidSortFieldException(String field) {
        super("Campo de ordenação inválido: " + field);
    }
}