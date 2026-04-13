package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.LoginRequestDTO;
import com.vitorino.apiveiculos.dto.LoginResponseDTO;
import com.vitorino.apiveiculos.exception.InvalidCredentialsException;
import com.vitorino.apiveiculos.model.User;
import com.vitorino.apiveiculos.repository.UserRepository;
import com.vitorino.apiveiculos.security.JWTService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;

    public LoginResponseDTO login(LoginRequestDTO dto) {
        String normalizedEmail = dto.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(InvalidCredentialsException::new);

        boolean passwordMatches = passwordEncoder.matches(dto.password(), user.getPasswordHash());

        if (!passwordMatches) {
            throw new InvalidCredentialsException();
        }

        String token = jwtService.generateToken(user);

        return new LoginResponseDTO(token, "Bearer");
    }
}