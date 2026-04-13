package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.LoginRequestDTO;
import com.vitorino.apiveiculos.dto.LoginResponseDTO;
import com.vitorino.apiveiculos.exception.InvalidCredentialsException;
import com.vitorino.apiveiculos.model.User;
import com.vitorino.apiveiculos.model.UserRole;
import com.vitorino.apiveiculos.repository.UserRepository;
import com.vitorino.apiveiculos.security.JWTService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JWTService jwtService;

    @InjectMocks
    private AuthService authService;

    @Nested
    class Login {

        @Test
        @DisplayName("Deve realizar login com sucesso")
        void shouldLoginSuccessfully() {
            LoginRequestDTO request = new LoginRequestDTO(
                    "user@example.com",
                    "12345678Ga@"
            );

            User user = User.builder()
                    .id(UUID.randomUUID())
                    .email("user@example.com")
                    .passwordHash("hashed-password")
                    .role(UserRole.USER)
                    .build();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("12345678Ga@", "hashed-password")).thenReturn(true);
            when(jwtService.generateToken(user)).thenReturn("jwt-token");

            LoginResponseDTO response = authService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwt-token");
            assertThat(response.type()).isEqualTo("Bearer");

            verify(userRepository).findByEmail("user@example.com");
            verify(passwordEncoder).matches("12345678Ga@", "hashed-password");
            verify(jwtService).generateToken(user);
        }

        @Test
        @DisplayName("Deve normalizar email antes de buscar usuário")
        void shouldNormalizeEmailBeforeFindingUser() {
            LoginRequestDTO request = new LoginRequestDTO(
                    "  USER@EXAMPLE.COM  ",
                    "12345678Ga@"
            );

            User user = User.builder()
                    .id(UUID.randomUUID())
                    .email("user@example.com")
                    .passwordHash("hashed-password")
                    .role(UserRole.USER)
                    .build();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("12345678Ga@", "hashed-password")).thenReturn(true);
            when(jwtService.generateToken(user)).thenReturn("jwt-token");

            LoginResponseDTO response = authService.login(request);

            assertThat(response.token()).isEqualTo("jwt-token");
            verify(userRepository).findByEmail("user@example.com");
        }

        @Test
        @DisplayName("Deve lançar exceção quando email não existir")
        void shouldThrowExceptionWhenEmailDoesNotExist() {
            LoginRequestDTO request = new LoginRequestDTO(
                    "user@example.com",
                    "12345678Ga@"
            );

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Email ou senha inválidos");

            verify(userRepository).findByEmail("user@example.com");
            verify(passwordEncoder, never()).matches(anyString(), anyString());
            verify(jwtService, never()).generateToken(any(User.class));
        }

        @Test
        @DisplayName("Deve lançar exceção quando senha estiver incorreta")
        void shouldThrowExceptionWhenPasswordIsInvalid() {
            LoginRequestDTO request = new LoginRequestDTO(
                    "user@example.com",
                    "senha-errada"
            );

            User user = User.builder()
                    .id(UUID.randomUUID())
                    .email("user@example.com")
                    .passwordHash("hashed-password")
                    .role(UserRole.USER)
                    .build();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senha-errada", "hashed-password")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class)
                    .hasMessage("Email ou senha inválidos");

            verify(userRepository).findByEmail("user@example.com");
            verify(passwordEncoder).matches("senha-errada", "hashed-password");
            verify(jwtService, never()).generateToken(any(User.class));
        }

        @Test
        @DisplayName("Não deve gerar token quando senha estiver incorreta")
        void shouldNotGenerateTokenWhenPasswordIsInvalid() {
            LoginRequestDTO request = new LoginRequestDTO(
                    "user@example.com",
                    "senha-errada"
            );

            User user = User.builder()
                    .id(UUID.randomUUID())
                    .email("user@example.com")
                    .passwordHash("hashed-password")
                    .role(UserRole.USER)
                    .build();

            when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senha-errada", "hashed-password")).thenReturn(false);

            assertThatThrownBy(() -> authService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);

            verify(jwtService, never()).generateToken(any(User.class));
        }
    }
}