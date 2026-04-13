package com.vitorino.apiveiculos.controller;

import com.vitorino.apiveiculos.dto.UserRequestDTO;
import com.vitorino.apiveiculos.dto.UserResponseDTO;
import com.vitorino.apiveiculos.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO create(@Valid @RequestBody UserRequestDTO request) {
        return userService.create(request);
    }
}