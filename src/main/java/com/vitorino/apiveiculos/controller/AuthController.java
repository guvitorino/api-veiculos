package com.vitorino.apiveiculos.controller;

import com.vitorino.apiveiculos.dto.LoginRequestDTO;
import com.vitorino.apiveiculos.dto.LoginResponseDTO;
import com.vitorino.apiveiculos.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponseDTO login(@Valid @RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }
}