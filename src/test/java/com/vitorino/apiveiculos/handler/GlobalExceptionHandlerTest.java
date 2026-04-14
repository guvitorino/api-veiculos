package com.vitorino.apiveiculos.handler;

import com.vitorino.apiveiculos.exception.EmailAlreadyExistsException;
import com.vitorino.apiveiculos.exception.ErrorResponse;
import com.vitorino.apiveiculos.exception.ExternalServiceException;
import com.vitorino.apiveiculos.exception.InvalidCredentialsException;
import com.vitorino.apiveiculos.exception.LicensePlateAlreadyExistsException;
import com.vitorino.apiveiculos.exception.ValidationErrorResponse;
import com.vitorino.apiveiculos.exception.VehicleNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
    }

    @Test
    @DisplayName("Deve retornar 400 com payload de erro de validacao")
    void shouldHandleValidationException() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "request");
        bindingResult.addError(new FieldError("request", "placa", "Placa e obrigatoria"));
        bindingResult.addError(new FieldError("request", "marca", "Marca e obrigatoria"));

        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ValidationErrorResponse> response = handler.handleValidationException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Erro de validação");
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getBody().timestamp()).isNotNull();
        assertThat(response.getBody().errors())
                .containsEntry("placa", "Placa e obrigatoria")
                .containsEntry("marca", "Marca e obrigatoria");
    }

    @Test
    @DisplayName("Deve retornar 409 para placa duplicada")
    void shouldHandleLicensePlateAlreadyExistsException() {
        LicensePlateAlreadyExistsException exception = new LicensePlateAlreadyExistsException("ABC1234");

        ResponseEntity<ErrorResponse> response = handler.handleLicensePlateExists(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("Placa já cadastrada").contains("ABC1234");
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 502 para falha em servico externo")
    void shouldHandleExternalServiceException() {
        ExternalServiceException exception = new ExternalServiceException("Erro ao consultar cotacao");

        ResponseEntity<ErrorResponse> response = handler.handleExternalServiceException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_GATEWAY);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("Erro ao consultar cotacao");
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.BAD_GATEWAY.value());
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 500 para excecao generica")
    void shouldHandleGenericException() {
        Exception exception = new Exception("falha inesperada");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("Erro interno do servidor").contains("falha inesperada");
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 404 para veiculo nao encontrado")
    void shouldHandleVehicleNotFoundException() {
        UUID id = UUID.randomUUID();
        VehicleNotFoundException exception = new VehicleNotFoundException(id);

        ResponseEntity<ErrorResponse> response = handler.handleVehicleNotFound(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("Veículo não encontrado").contains(id.toString());
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 409 para email duplicado")
    void shouldHandleEmailAlreadyExistsException() {
        EmailAlreadyExistsException exception = new EmailAlreadyExistsException("user@example.com");

        ResponseEntity<ErrorResponse> response = handler.handleEmailAlreadyExistsException(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("usuário cadastrado").contains("user@example.com");
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 401 para credenciais invalidas")
    void shouldHandleInvalidCredentialsException() {
        InvalidCredentialsException exception = new InvalidCredentialsException();

        ResponseEntity<ErrorResponse> response = handler.handleInvalidCredentials(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).contains("Email ou senha inválidos");
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar 400 para JSON invalido")
    void shouldHandleHttpMessageNotReadableException() {
        HttpMessageNotReadableException exception =
                new HttpMessageNotReadableException("JSON inválido", mock(HttpInputMessage.class));

        ResponseEntity<Map<String, String>> response = handler.handleHttpMessageNotReadable(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "JSON inválido ou mal formatado"));
    }

    @Test
    @DisplayName("Deve retornar 403 para acesso negado")
    void shouldHandleAuthorizationDeniedException() {
        AuthorizationDeniedException exception = new AuthorizationDeniedException("Forbidden");

        ResponseEntity<ErrorResponse> response = handler.handleAuthorizationDenied(exception);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().message()).isEqualTo("Forbidden");
        assertThat(response.getBody().status()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getBody().timestamp()).isNotNull();
    }
}
