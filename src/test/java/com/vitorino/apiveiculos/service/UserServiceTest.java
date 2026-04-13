package com.vitorino.apiveiculos.service;

import com.vitorino.apiveiculos.dto.UserRequestDTO;
import com.vitorino.apiveiculos.dto.UserResponseDTO;
import com.vitorino.apiveiculos.model.UserRole;
import com.vitorino.apiveiculos.exception.EmailAlreadyExistsException;
import com.vitorino.apiveiculos.mapper.UserMapper;
import com.vitorino.apiveiculos.model.User;
import com.vitorino.apiveiculos.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Nested
    class Create {

        @Test
        @DisplayName("Deve criar usuário com sucesso")
        void shouldCreateUserSuccessfully() {
            UserRequestDTO request = new UserRequestDTO(
                    "user@email.com",
                    "Admin123",
                    UserRole.USER
            );

            User savedUser = User.builder()
                    .id(UUID.randomUUID())
                    .email("user@email.com")
                    .passwordHash("hashed-password")
                    .role(UserRole.USER)
                    .build();

            UserResponseDTO response = new UserResponseDTO(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getRole()
            );

            when(userRepository.existsByEmail("user@email.com")).thenReturn(false);
            when(passwordEncoder.encode("Admin123")).thenReturn("hashed-password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toResponseDTO(savedUser)).thenReturn(response);

            UserResponseDTO result = userService.create(request);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(savedUser.getId());
            assertThat(result.email()).isEqualTo("user@email.com");
            assertThat(result.role()).isEqualTo(UserRole.USER);

            verify(userRepository).existsByEmail("user@email.com");
            verify(passwordEncoder).encode("Admin123");
            verify(userRepository).save(any(User.class));
            verify(userMapper).toResponseDTO(savedUser);
        }

        @Test
        @DisplayName("Deve normalizar email antes de verificar existência e salvar")
        void shouldNormalizeEmailBeforeCheckingAndSaving() {
            UserRequestDTO request = new UserRequestDTO(
                    "  TEST@EMAIL.COM  ",
                    "Admin123",
                    UserRole.ADMIN
            );

            User savedUser = User.builder()
                    .id(UUID.randomUUID())
                    .email("test@email.com")
                    .passwordHash("hashed-password")
                    .role(UserRole.ADMIN)
                    .build();

            UserResponseDTO response = new UserResponseDTO(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getRole()
            );

            when(userRepository.existsByEmail("test@email.com")).thenReturn(false);
            when(passwordEncoder.encode("Admin123")).thenReturn("hashed-password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toResponseDTO(savedUser)).thenReturn(response);

            userService.create(request);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();

            assertThat(capturedUser.getEmail()).isEqualTo("test@email.com");
            assertThat(capturedUser.getRole()).isEqualTo(UserRole.ADMIN);

            verify(userRepository).existsByEmail("test@email.com");
        }

        @Test
        @DisplayName("Deve salvar senha com hash")
        void shouldEncodePasswordBeforeSaving() {
            UserRequestDTO request = new UserRequestDTO(
                    "user@email.com",
                    "Admin123",
                    UserRole.USER
            );

            when(userRepository.existsByEmail("user@email.com")).thenReturn(false);
            when(passwordEncoder.encode("Admin123")).thenReturn("hashed-password");

            User savedUser = User.builder()
                    .id(UUID.randomUUID())
                    .email("user@email.com")
                    .passwordHash("hashed-password")
                    .role(UserRole.USER)
                    .build();

            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toResponseDTO(savedUser)).thenReturn(
                    new UserResponseDTO(savedUser.getId(), savedUser.getEmail(), savedUser.getRole())
            );

            userService.create(request);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            User capturedUser = userCaptor.getValue();

            assertThat(capturedUser.getPasswordHash()).isEqualTo("hashed-password");
            assertThat(capturedUser.getPasswordHash()).isNotEqualTo("Admin123");

            verify(passwordEncoder).encode("Admin123");
        }

        @Test
        @DisplayName("Deve lançar exceção quando email já existir")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            UserRequestDTO request = new UserRequestDTO(
                    "user@email.com",
                    "Admin123",
                    UserRole.USER
            );

            when(userRepository.existsByEmail("user@email.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.create(request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("Já existe um usuário cadastrado com o email: user@email.com");

            verify(userRepository).existsByEmail("user@email.com");
            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
            verify(userMapper, never()).toResponseDTO(any(User.class));
        }

        @Test
        @DisplayName("Deve chamar mapper com usuário salvo")
        void shouldCallMapperWithSavedUser() {
            UserRequestDTO request = new UserRequestDTO(
                    "user@email.com",
                    "Admin123",
                    UserRole.USER
            );

            User savedUser = User.builder()
                    .id(UUID.randomUUID())
                    .email("user@email.com")
                    .passwordHash("hashed-password")
                    .role(UserRole.USER)
                    .build();

            UserResponseDTO response = new UserResponseDTO(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    savedUser.getRole()
            );

            when(userRepository.existsByEmail("user@email.com")).thenReturn(false);
            when(passwordEncoder.encode("Admin123")).thenReturn("hashed-password");
            when(userRepository.save(any(User.class))).thenReturn(savedUser);
            when(userMapper.toResponseDTO(savedUser)).thenReturn(response);

            userService.create(request);

            verify(userMapper).toResponseDTO(savedUser);
        }
    }
}