package com.vitorino.apiveiculos.controller;

import com.vitorino.apiveiculos.dto.LoginResponseDTO;
import com.vitorino.apiveiculos.dto.VehicleResponsetDTO;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.reactive.server.WebTestClient.bindToServer;

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
        String licensePlate = "ABC1234";
        String token = bearerTokenFor(UserRole.ADMIN);
        vehicleRepository.save(existingVehicle(licensePlate));

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
                .jsonPath("$.modelo").isEqualTo("Fox")
                .jsonPath("$.ano").isEqualTo(2020)
                .jsonPath("$.cor").isEqualTo("Prata")
                .jsonPath("$.preco").isEqualTo(5000.00);
    }

    @Test
    @DisplayName("Deve executar fluxo ponta a ponta de obter token criar listar filtrar e detalhar veículo")
    void shouldExecuteEndToEndFlowFromLoginToVehicleDetail() {
        String password = "123456";
        User admin = userRepository.save(User.builder()
                .email("admin@example.com")
                .passwordHash(passwordEncoder.encode(password))
                .role(UserRole.ADMIN)
                .build());

        String token = loginAndGetAuthorizationHeader(admin.getEmail(), password);

        VehicleResponsetDTO createdVehicle = webTestClient.post()
                .uri("/veiculos")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(VehicleResponsetDTO.class)
                .returnResult()
                .getResponseBody();

        String createdId = createdVehicle.id().toString();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/veiculos")
                        .queryParam("marca", "Volkswagen")
                        .queryParam("cor", "Prata")
                        .queryParam("ano", 2020)
                        .build())
                .header("Authorization", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.length()").isEqualTo(1)
                .jsonPath("$.data[0].id").isEqualTo(createdId)
                .jsonPath("$.data[0].placa").isEqualTo("ABC1234")
                .jsonPath("$.data[0].marca").isEqualTo("Volkswagen")
                .jsonPath("$.totalElements").isEqualTo(1)
                .jsonPath("$.page").isEqualTo(0);

        webTestClient.get()
                .uri("/veiculos/{id}", UUID.fromString(createdId))
                .header("Authorization", token)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.id").isEqualTo(createdId)
                .jsonPath("$.placa").isEqualTo("ABC1234")
                .jsonPath("$.marca").isEqualTo("Volkswagen")
                .jsonPath("$.modelo").isEqualTo("Fox")
                .jsonPath("$.ano").isEqualTo(2020)
                .jsonPath("$.cor").isEqualTo("Prata")
                .jsonPath("$.preco").isEqualTo(5000.00);
    }

    @Test
    @DisplayName("Deve permitir reutilizar placa apos soft delete")
    void shouldAllowReusingLicensePlateAfterSoftDelete() {
        String token = bearerTokenFor(UserRole.ADMIN);

        VehicleResponsetDTO createdVehicle = webTestClient.post()
                .uri("/veiculos")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody(VehicleResponsetDTO.class)
                .returnResult()
                .getResponseBody();

        webTestClient.delete()
                .uri("/veiculos/{id}", createdVehicle.id())
                .header("Authorization", token)
                .exchange()
                .expectStatus().isNoContent();

        webTestClient.post()
                .uri("/veiculos")
                .header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(validRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.placa").isEqualTo("ABC1234");
    }

    private String bearerTokenFor(UserRole role) {
        String password = "123456";
        User user = userRepository.save(User.builder()
                .email(role.name().toLowerCase() + "@example.com")
                .passwordHash(passwordEncoder.encode(password))
                .role(role)
                .build());

        return "Bearer " + jwtService.generateToken(user);
    }

    private String loginAndGetAuthorizationHeader(String email, String password) {
        LoginResponseDTO response = webTestClient.post()
                .uri("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of(
                        "email", email,
                        "password", password
                ))
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginResponseDTO.class)
                .returnResult()
                .getResponseBody();

        return response.type() + " " + response.token();
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
