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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
    @DisplayName("Deve realizar login com sucesso")
    void shouldLoginSuccessfully() {
        userRepository.save(User.builder()
                .email("user@example.com")
                .passwordHash(passwordEncoder.encode("Admin123"))
                .role(UserRole.USER)
                .build());

        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "email", "user@example.com",
                        "password", "Admin123"
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.type").isEqualTo("Bearer")
                .jsonPath("$.token").isNotEmpty();
    }

    @Test
    @DisplayName("Deve retornar 401 com payload padronizado quando credenciais forem inválidas")
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() {
        userRepository.save(User.builder()
                .email("user@example.com")
                .passwordHash(passwordEncoder.encode("Admin123"))
                .role(UserRole.USER)
                .build());

        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "email", "user@example.com",
                        "password", "SenhaErrada1"
                ))
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Email ou senha inválidos")
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.timestamp").exists();
    }

    @Test
    @DisplayName("Deve retornar 400 com payload de validação quando login for inválido")
    void shouldReturnBadRequestWhenLoginPayloadIsInvalid() {
        webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "email", "email-invalido",
                        "password", ""
                ))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Erro de validação")
                .jsonPath("$.status").isEqualTo(400)
                .jsonPath("$.timestamp").exists()
                .jsonPath("$.errors.email").isEqualTo("Email inválido")
                .jsonPath("$.errors.password").isEqualTo("Senha é obrigatória");
    }
}
