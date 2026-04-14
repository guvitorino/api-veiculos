package com.vitorino.apiveiculos.controller;

import com.vitorino.apiveiculos.model.User;
import com.vitorino.apiveiculos.model.UserRole;
import com.vitorino.apiveiculos.model.Vehicle;
import com.vitorino.apiveiculos.repository.UserRepository;
import com.vitorino.apiveiculos.repository.VehicleRepository;
import com.vitorino.apiveiculos.security.JWTService;
import com.vitorino.apiveiculos.service.CurrencyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class VehicleControllerIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTService jwtService;

    @LocalServerPort
    private int port;

    @MockitoBean
    private CurrencyService currencyService;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
        userRepository.deleteAll();
        given(currencyService.convertBrlToUsd(any(BigDecimal.class))).willReturn(new BigDecimal("5000.00"));
        webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }

    @Test
    @DisplayName("Deve retornar 401 com payload padronizado quando não houver autenticação")
    void shouldReturnUnauthorizedWhenRequestHasNoAuthentication() throws Exception {
        webTestClient.post()
                .uri("/veiculos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest())
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Unauthorized")
                .jsonPath("$.status").isEqualTo(401)
                .jsonPath("$.timestamp").exists();
    }

    @Test
    @DisplayName("Deve retornar 403 com payload padronizado quando usuário não for admin")
    void shouldReturnForbiddenWhenUserDoesNotHaveAdminRole() throws Exception {
        String token = bearerTokenFor(UserRole.USER);

        webTestClient.post()
                .uri("/veiculos")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest())
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Forbidden")
                .jsonPath("$.status").isEqualTo(403)
                .jsonPath("$.timestamp").exists();
    }

    @Test
    @DisplayName("Deve retornar 409 com payload padronizado quando a placa já estiver cadastrada")
    void shouldReturnConflictWhenLicensePlateAlreadyExists() throws Exception {
        String token = bearerTokenFor(UserRole.ADMIN);
        vehicleRepository.save(existingVehicle("ABC1234"));

        webTestClient.post()
                .uri("/veiculos")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest())
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.message").isEqualTo("Placa já cadastrada: ABC1234")
                .jsonPath("$.status").isEqualTo(409)
                .jsonPath("$.timestamp").exists();
    }

    @Test
    @DisplayName("Deve cadastrar veículo com sucesso quando usuário for admin")
    void shouldCreateVehicleSuccessfullyWhenUserIsAdmin() throws Exception {
        String token = bearerTokenFor(UserRole.ADMIN);

        webTestClient.post()
                .uri("/veiculos")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").exists()
                .jsonPath("$.placa").isEqualTo("ABC1234")
                .jsonPath("$.marca").isEqualTo("Volkswagen")
                .jsonPath("$.modelo").isEqualTo("Gol")
                .jsonPath("$.ano").isEqualTo(2020)
                .jsonPath("$.cor").isEqualTo("Prata")
                .jsonPath("$.preco").isEqualTo(5000.00);
    }

    private String bearerTokenFor(UserRole role) {
        User user = userRepository.save(User.builder()
                .email(role.name().toLowerCase() + "@example.com")
                .passwordHash(passwordEncoder.encode("123456"))
                .role(role)
                .build());

        return "Bearer " + jwtService.generateToken(user);
    }

    private Vehicle existingVehicle(String licensePlate) {
        Vehicle vehicle = new Vehicle();
        vehicle.setLicensePlate(licensePlate);
        vehicle.setBrand("Fiat");
        vehicle.setModel("Uno");
        vehicle.setVehicleYear(2018);
        vehicle.setColor("Branco");
        vehicle.setPrice(new BigDecimal("3500.00"));
        vehicle.setDeleted(false);
        return vehicle;
    }

    private Map<String, Object> validRequest() {
        return Map.of(
                "placa", "ABC1234",
                "marca", "Volkswagen",
                "modelo", "Fox",
                "ano", 2020,
                "cor", "Prata",
                "preco", new BigDecimal("25000.00")
        );
    }
}
