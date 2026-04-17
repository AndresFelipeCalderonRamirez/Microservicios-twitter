package tdse.lab.twitter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tdse.lab.twitter.model.User;
import tdse.lab.twitter.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — Pruebas Unitarias")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User("Juan Pérez", "juan@gmail.com", "123456");
        mockUser.setId(1L);
    }

    // ─────────────────────────────────────────────────────────────────────
    // createUser
    // ─────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createUser()")
    class CreateUser {

        @Test
        @DisplayName("✅ Crea usuario correctamente con datos válidos")
        void createUser_success() {
            when(userRepository.existsByEmail("juan@gmail.com")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(mockUser);

            User result = userService.createUser("Juan Pérez", "juan@gmail.com", "123456");

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Juan Pérez");
            assertThat(result.getEmail()).isEqualTo("juan@gmail.com");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Lanza excepción si el email ya está registrado")
        void createUser_emailAlreadyExists_throwsException() {
            when(userRepository.existsByEmail("juan@gmail.com")).thenReturn(true);

            assertThatThrownBy(() ->
                    userService.createUser("Juan Pérez", "juan@gmail.com", "123456")
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Email already registered");

            verify(userRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // authenticate
    // ─────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("authenticate()")
    class Authenticate {

        @Test
        @DisplayName("Autentica usuario con credenciales correctas")
        void authenticate_success() {
            when(userRepository.findByEmail("juan@gmail.com")).thenReturn(Optional.of(mockUser));

            User result = userService.authenticate("juan@gmail.com", "123456");

            assertThat(result).isEqualTo(mockUser);
        }

        @Test
        @DisplayName("Lanza excepción si el email no existe")
        void authenticate_emailNotFound_throwsException() {
            when(userRepository.findByEmail("noexiste@gmail.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    userService.authenticate("noexiste@gmail.com", "123456")
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");
        }

        @Test
        @DisplayName("Lanza excepción si la contraseña es incorrecta")
        void authenticate_wrongPassword_throwsException() {
            when(userRepository.findByEmail("juan@gmail.com")).thenReturn(Optional.of(mockUser));

            assertThatThrownBy(() ->
                    userService.authenticate("juan@gmail.com", "wrongpass")
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Invalid credentials");
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // findAll / findByName / findById
    // ─────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Consultas")
    class Queries {

        @Test
        @DisplayName("findAll() retorna lista de usuarios")
        void findAll_returnsAllUsers() {
            User user2 = new User("María", "maria@gmail.com", "654321");
            user2.setId(2L);
            when(userRepository.findAll()).thenReturn(List.of(mockUser, user2));

            List<User> result = userService.findAll();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(User::getName)
                    .containsExactly("Juan Pérez", "María");
        }

        @Test
        @DisplayName("findAll() retorna lista vacía cuando no hay usuarios")
        void findAll_empty() {
            when(userRepository.findAll()).thenReturn(List.of());

            assertThat(userService.findAll()).isEmpty();
        }

        @Test
        @DisplayName("findByName() retorna usuarios que coinciden con el nombre")
        void findByName_returnsMatchingUsers() {
            when(userRepository.findByNameContainingIgnoreCase("juan"))
                    .thenReturn(List.of(mockUser));

            List<User> result = userService.findByName("juan");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Juan Pérez");
        }

        @Test
        @DisplayName("findById() retorna el usuario correcto")
        void findById_returnsUser() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

            Optional<User> result = userService.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("findById() retorna empty si no existe")
        void findById_notFound_returnsEmpty() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThat(userService.findById(99L)).isEmpty();
        }
    }
}