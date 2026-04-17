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
import tdse.lab.twitter.dto.CreatePostRequest;
import tdse.lab.twitter.model.Post;
import tdse.lab.twitter.model.Stream;
import tdse.lab.twitter.model.User;
import tdse.lab.twitter.service.PostService;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
@Import(SecurityConfig.class)
@DisplayName("PostController — Pruebas de Integración")
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PostService postService;

    @MockBean
    private JwtDecoder jwtDecoder;   // ← evita que Spring intente conectarse a Auth0

    private User   testUser;
    private Stream globalStream;
    private Post   testPost;

    @BeforeEach
    void setUp() {
        testUser = new User("Juan Pérez", "juan@gmail.com", "123456");
        testUser.setId(1L);

        globalStream = new Stream("global");
        globalStream.setId(1L);

        testPost = new Post("Hola mundo!", testUser);
        testPost.setId(1L);
        testPost.setStream(globalStream);
    }

    // ─────────────────────────────────────────────────────────────────────
    // POST /api/posts  (protegido)
    // ─────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/posts")
    class CreatePost {

        @Test
        @DisplayName("201 — crea post con JWT válido")
        void createPost_withValidJwt_returns201() throws Exception {
            when(postService.createPost("Hola mundo!", 1L)).thenReturn(testPost);

            CreatePostRequest request = new CreatePostRequest();
            request.setContent("Hola mundo!");
            request.setUserId(1L);

            mockMvc.perform(post("/api/posts")
                            .with(jwt())                                   // JWT sintético
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.content").value("Hola mundo!"));
        }

        @Test
        @DisplayName("401 — sin JWT retorna Unauthorized")
        void createPost_withoutJwt_returns401() throws Exception {
            CreatePostRequest request = new CreatePostRequest();
            request.setContent("Hola mundo!");
            request.setUserId(1L);

            mockMvc.perform(post("/api/posts")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("400 — contenido vacío con JWT válido")
        void createPost_emptyContent_returns400() throws Exception {
            CreatePostRequest request = new CreatePostRequest();
            request.setContent("");           // @NotBlank fallará
            request.setUserId(1L);

            mockMvc.perform(post("/api/posts")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 — userId null con JWT válido")
        void createPost_nullUserId_returns400() throws Exception {
            CreatePostRequest request = new CreatePostRequest();
            request.setContent("Contenido válido");
            // userId no se establece → null → @NotNull fallará

            mockMvc.perform(post("/api/posts")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("400 — usuario no encontrado")
        void createPost_userNotFound_returns400() throws Exception {
            when(postService.createPost(anyString(), eq(99L)))
                    .thenThrow(new RuntimeException("User not found"));

            CreatePostRequest request = new CreatePostRequest();
            request.setContent("Hola mundo!");
            request.setUserId(99L);

            mockMvc.perform(post("/api/posts")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error").value("User not found"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/posts  (público)
    // ─────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/posts")
    class GetAllPosts {

        @Test
        @DisplayName("200 — retorna lista de posts sin token")
        void getAllPosts_public_returns200() throws Exception {
            when(postService.getAllPosts()).thenReturn(List.of(testPost));

            mockMvc.perform(get("/api/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].content").value("Hola mundo!"));
        }

        @Test
        @DisplayName("200 — retorna lista vacía cuando no hay posts")
        void getAllPosts_emptyList_returns200() throws Exception {
            when(postService.getAllPosts()).thenReturn(List.of());

            mockMvc.perform(get("/api/posts"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/posts/stream  (público)
    // ─────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/posts/stream")
    class GetGlobalStream {

        @Test
        @DisplayName("200 — retorna posts del stream global sin token")
        void getGlobalStream_public_returns200() throws Exception {
            when(postService.getGlobalStreamPosts()).thenReturn(List.of(testPost));

            mockMvc.perform(get("/api/posts/stream"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].id").value(1));
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/posts/{id}  (público)
    // ─────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/posts/{id}")
    class GetPostById {

        @Test
        @DisplayName("200 — post encontrado")
        void getPostById_found_returns200() throws Exception {
            when(postService.findById(1L)).thenReturn(Optional.of(testPost));

            mockMvc.perform(get("/api/posts/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.content").value("Hola mundo!"));
        }

        @Test
        @DisplayName("404 — post no encontrado")
        void getPostById_notFound_returns404() throws Exception {
            when(postService.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/posts/999"))
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // GET /api/posts/user?name=  (público)
    // ─────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/posts/user?name=")
    class GetPostsByUserName {

        @Test
        @DisplayName("200 — posts encontrados por nombre de autor")
        void getPostsByUserName_found_returns200() throws Exception {
            when(postService.getPostsByUserName("Juan")).thenReturn(List.of(testPost));

            mockMvc.perform(get("/api/posts/user").param("name", "Juan"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        @DisplayName("404 — ningún post del autor buscado")
        void getPostsByUserName_notFound_returns404() throws Exception {
            when(postService.getPostsByUserName("Fantasma")).thenReturn(List.of());

            mockMvc.perform(get("/api/posts/user").param("name", "Fantasma"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("No posts found for user: Fantasma"));
        }
    }
}