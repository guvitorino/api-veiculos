package com.vitorino.apiveiculos.dto;

import com.vitorino.apiveiculos.model.UserRole;

import java.util.UUID;

public record UserResponseDTO(
        UUID id,
        String email,
        UserRole role
) {}