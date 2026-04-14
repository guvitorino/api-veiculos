package com.vitorino.apiveiculos.controller;

import com.vitorino.apiveiculos.model.User;
import com.vitorino.apiveiculos.model.UserRole;
import com.vitorino.apiveiculos.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validUserRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.email").isEqualTo("admin@example.com")
                .jsonPath("$.role").isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("Deve retornar 409 com payload padronizado quando email já existir")
    void shouldReturnConflictWhenEmailAlreadyExists() {
        userRepository.save(User.builder()
                .email("admin@example.com")
                .passwordHash("hash")
                .role(UserRole.ADMIN)
                .build());

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validUserRequest())
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Já existe um usuário cadastrado com o email: admin@example.com")
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.timestamp").exists();
    }

    @Test
    @DisplayName("Deve retornar 400 com payload de validação quando request for inválido")
    void shouldReturnBadRequestWithValidationPayload() {
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "email", "invalido",
                        "password", "123",
                        "role", "ADMIN"
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Erro de validação")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.errors.email").isEqualTo("Email inválido")
                .jsonPath("$.errors.password").exists();
    }

    private Map<String, Object> validUserRequest() {
        return Map.of(
                "email", "admin@example.com",
                "password", "Admin123",
                "role", "ADMIN"
        );
    }
}
