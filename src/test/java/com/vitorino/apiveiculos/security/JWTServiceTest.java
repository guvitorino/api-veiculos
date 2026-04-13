package com.vitorino.apiveiculos.security;

import com.vitorino.apiveiculos.model.User;
import com.vitorino.apiveiculos.model.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class JWTServiceTest {

    private JWTService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();

        ReflectionTestUtils.setField(
                jwtService,
                "jwtSecret",
                "ZGV2ZWxvcGltZW50ZGV2ZWxvcGltZW50ZGV2ZWxvcGltZW50MTIzNDU2"
        );
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);

        user = User.builder()
                .id(UUID.randomUUID())
                .email("user@example.com")
                .passwordHash("hashed-password")
                .role(UserRole.USER)
                .build();
    }

    @Nested
    class GenerateToken {

        @Test
        @DisplayName("Deve gerar token com sucesso")
        void shouldGenerateTokenSuccessfully() {
            String token = jwtService.generateToken(user);

            assertThat(token).isNotNull();
            assertThat(token).isNotBlank();
        }

        @Test
        @DisplayName("Deve gerar token com o email como subject")
        void shouldGenerateTokenWithEmailAsSubject() {
            String token = jwtService.generateToken(user);

            String extractedUsername = jwtService.extractUsername(token);

            assertThat(extractedUsername).isEqualTo(user.getEmail());
        }
    }

    @Nested
    class ExtractUsername {

        @Test
        @DisplayName("Deve extrair username do token")
        void shouldExtractUsernameFromToken() {
            String token = jwtService.generateToken(user);

            String username = jwtService.extractUsername(token);

            assertThat(username).isEqualTo("user@example.com");
        }
    }

    @Nested
    class IsTokenValid {

        @Test
        @DisplayName("Deve retornar true quando token for válido")
        void shouldReturnTrueWhenTokenIsValid() {
            String token = jwtService.generateToken(user);

            boolean isValid = jwtService.isTokenValid(token, "user@example.com");

            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando token pertencer a outro usuário")
        void shouldReturnFalseWhenTokenBelongsToAnotherUser() {
            String token = jwtService.generateToken(user);

            boolean isValid = jwtService.isTokenValid(token, "admin@example.com");

            assertThat(isValid).isFalse();
        }

        @Test
        @DisplayName("Deve lançar exceção quando token estiver expirado")
        void shouldThrowExceptionWhenTokenIsExpired() {
            ReflectionTestUtils.setField(jwtService, "jwtExpiration", -1000L);

            String token = jwtService.generateToken(user);

            assertThatThrownBy(() -> jwtService.isTokenValid(token, "user@example.com"))
                    .isInstanceOf(ExpiredJwtException.class);
        }
    }
}