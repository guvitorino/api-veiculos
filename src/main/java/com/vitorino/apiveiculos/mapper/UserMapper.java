package com.vitorino.apiveiculos.mapper;

import com.vitorino.apiveiculos.dto.UserResponseDTO;
import com.vitorino.apiveiculos.model.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponseDTO toResponseDTO(User user);
}