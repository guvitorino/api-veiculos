package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.UserRequestDTO;
import com.vitorino.apiveiculos.dto.UserResponseDTO;
import com.vitorino.apiveiculos.model.User;
import com.vitorino.apiveiculos.exception.EmailAlreadyExistsException;
import com.vitorino.apiveiculos.mapper.UserMapper;
import com.vitorino.apiveiculos.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponseDTO create(UserRequestDTO dto) {
        String normalizedEmail = dto.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        User user = User.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(dto.password()))
                .role(dto.role())
                .build();

        User savedUser = userRepository.save(user);

        return userMapper.toResponseDTO(savedUser);
    }
}