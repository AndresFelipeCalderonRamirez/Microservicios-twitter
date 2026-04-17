package tdse.lab.twitter.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import tdse.lab.twitter.config.SecurityConfig;
import tdse.lab.twitter.dto.LoginRequest;
import tdse.lab.twitter.dto.RegisterRequest;
import tdse.lab.twitter.model.User;
import tdse.lab.twitter.service.UserService;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// ══════════════════════════════════════════════════════════════════════════
// USER CONTROLLER
// ══════════════════════════════════════════════════════════════════════════
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@DisplayName("UserController — Pruebas de Integración")
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserService userService;
    @MockBean private JwtDecoder  jwtDecoder;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Juan Pérez", "juan@gmail.com", "123456");
        testUser.setId(1L);
    }

    // ── POST /api/users/register ─────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/users/register")
    class Register {

        @Test
        @DisplayName("201 — registro exitoso")
        void register_success_returns201() throws Exception {
            when(userService.createUser("Juan Pérez", "juan@gmail.com", "123456"))
                    .thenReturn(testUser);

            RegisterRequest req = buildRegisterRequest("Juan Pérez", "juan@gmail.com", "123456");

            mockMvc.perform(post("/api/users/register")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.email").value("juan@gmail.com"));
        }

        @Test
        @DisplayName("400 — email ya registrado")
        void register_duplicateEmail_returns400() throws Exception {
            when(userService.createUser(anyString(), eq("juan@gmail.com"), anyString()))
                    .thenThrow(new RuntimeException("Email already registered"));

            RegisterRequest req = buildRegisterRequest("Otro", "juan@gmail.com", "654321");

            mockMvc.perform(post("/api/users/register")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("Email already registered"));
        }

        @Test
        @DisplayName("401 — sin JWT retorna Unauthorized")
        void register_withoutJwt_returns401() throws Exception {
            RegisterRequest req = buildRegisterRequest("Juan", "juan@gmail.com", "123456");

            mockMvc.perform(post("/api/users/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── POST /api/users/login ────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/users/login")
    class Login {

        @Test
        @DisplayName("200 — login exitoso")
        void login_success_returns200() throws Exception {
            when(userService.authenticate("juan@gmail.com", "123456")).thenReturn(testUser);

            LoginRequest req = buildLoginRequest("juan@gmail.com", "123456");

            mockMvc.perform(post("/api/users/login")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("401 — contraseña incorrecta")
        void login_wrongPassword_returns401() throws Exception {
            when(userService.authenticate("juan@gmail.com", "wrong"))
                    .thenThrow(new RuntimeException("Invalid credentials"));

            LoginRequest req = buildLoginRequest("juan@gmail.com", "wrong");

            mockMvc.perform(post("/api/users/login")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(req)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));
        }
    }

    // ── GET /api/users ───────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsers {

        @Test
        @DisplayName("200 — retorna lista de usuarios con JWT")
        void getAllUsers_withJwt_returns200() throws Exception {
            when(userService.findAll()).thenReturn(List.of(testUser));

            mockMvc.perform(get("/api/users").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("401 — sin JWT retorna Unauthorized")
        void getAllUsers_withoutJwt_returns401() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // ── GET /api/users/search?name= ──────────────────────────────────────
    @Nested
    @DisplayName("GET /api/users/search?name=")
    class SearchByName {

        @Test
        @DisplayName("200 — usuarios encontrados")
        void searchByName_found_returns200() throws Exception {
            when(userService.findByName("juan")).thenReturn(List.of(testUser));

            mockMvc.perform(get("/api/users/search").param("name", "juan").with(jwt()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].name").value("Juan Pérez"));
        }

        @Test
        @DisplayName("404 — ningún usuario con ese nombre")
        void searchByName_notFound_returns404() throws Exception {
            when(userService.findByName("XYZ")).thenReturn(List.of());

            mockMvc.perform(get("/api/users/search").param("name", "XYZ").with(jwt()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("No users found with name: XYZ"));
        }
    }

    // ── helpers ──────────────────────────────────────────────────────────
    private RegisterRequest buildRegisterRequest(String name, String email, String password) {
        RegisterRequest r = new RegisterRequest();
        r.setName(name);
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }

    private LoginRequest buildLoginRequest(String email, String password) {
        LoginRequest r = new LoginRequest();
        r.setEmail(email);
        r.setPassword(password);
        return r;
    }
}


