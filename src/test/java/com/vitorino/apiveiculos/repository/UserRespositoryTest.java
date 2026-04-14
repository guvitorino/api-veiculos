package com.vitorino.apiveiculos.repository;

import com.vitorino.apiveiculos.config.FlywayConfig;
import com.vitorino.apiveiculos.model.User;
import com.vitorino.apiveiculos.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(FlywayConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User createUser(String email) {
        return User.builder()
                .email(email)
                .passwordHash("hashed-password")
                .role(UserRole.USER)
                .build();
    }

    @Nested
    class ExistsByEmail {

        @Test
        @DisplayName("Deve retornar true quando email existir")
        void shouldReturnTrueWhenEmailExists() {
            User user = createUser("test@email.com");
            userRepository.save(user);

            boolean exists = userRepository.existsByEmail("test@email.com");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando email não existir")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            boolean exists = userRepository.existsByEmail("naoexiste@email.com");

            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("Deve considerar email normalizado (lowercase)")
        void shouldWorkWithLowercaseEmail() {
            User user = createUser("TEST@EMAIL.COM");
            userRepository.save(user);

            boolean exists = userRepository.existsByEmail("test@email.com");

            assertThat(exists).isTrue();
        }
    }

    @Nested
    class FindByEmail {

        @Test
        @DisplayName("Deve retornar usuário quando email existir")
        void shouldReturnUserWhenEmailExists() {
            User user = createUser("user@email.com");
            userRepository.save(user);

            Optional<User> result = userRepository.findByEmail("user@email.com");

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("user@email.com");
        }

        @Test
        @DisplayName("Deve retornar vazio quando email não existir")
        void shouldReturnEmptyWhenEmailDoesNotExist() {
            Optional<User> result = userRepository.findByEmail("naoexiste@email.com");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar corretamente com email normalizado")
        void shouldFindUserWithNormalizedEmail() {
            User user = createUser("TEST@EMAIL.COM");
            userRepository.save(user);

            Optional<User> result = userRepository.findByEmail("test@email.com");

            assertThat(result).isPresent();
        }
    }
}
