package com.vitorino.apiveiculos.dto;

import java.util.List;

public record ListPageResponseDTO<T>(
        List<T> data,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext,
        boolean hasPrevious
) {
}