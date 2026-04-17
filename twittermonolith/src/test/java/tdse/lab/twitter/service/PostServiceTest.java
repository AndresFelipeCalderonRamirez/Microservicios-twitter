package tdse.lab.twitter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tdse.lab.twitter.model.Post;
import tdse.lab.twitter.model.Stream;
import tdse.lab.twitter.model.User;
import tdse.lab.twitter.repository.PostRepository;
import tdse.lab.twitter.repository.StreamRepository;
import tdse.lab.twitter.repository.UserRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PostService — Pruebas Unitarias")
class PostServiceTest {

    @Mock private PostRepository   postRepository;
    @Mock private StreamRepository streamRepository;
    @Mock private UserRepository   userRepository;

    @InjectMocks
    private PostService postService;

    private User   mockUser;
    private Stream globalStream;
    private Post   mockPost;

    @BeforeEach
    void setUp() {
        mockUser = new User("Juan Pérez", "juan@gmail.com", "123456");
        mockUser.setId(1L);

        globalStream = new Stream("global");
        globalStream.setId(1L);

        mockPost = new Post("Hola mundo!", mockUser);
        mockPost.setId(1L);
        mockPost.setStream(globalStream);
    }

    // ─────────────────────────────────────────────────────────────────────
    // createPost
    // ─────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("createPost()")
    class CreatePost {

        @Test
        @DisplayName("Crea un post y lo asocia al stream global")
        void createPost_success() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(streamRepository.findByName("global")).thenReturn(Optional.of(globalStream));
            when(postRepository.save(any(Post.class))).thenReturn(mockPost);

            Post result = postService.createPost("Hola mundo!", 1L);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEqualTo("Hola mundo!");
            assertThat(result.getUser()).isEqualTo(mockUser);
            assertThat(result.getStream()).isEqualTo(globalStream);
            verify(postRepository).save(any(Post.class));
        }

        @Test
        @DisplayName("Crea el stream global si no existe")
        void createPost_createsGlobalStreamIfMissing() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(streamRepository.findByName("global")).thenReturn(Optional.empty());
            when(streamRepository.save(any(Stream.class))).thenReturn(globalStream);
            when(postRepository.save(any(Post.class))).thenReturn(mockPost);

            Post result = postService.createPost("Hola mundo!", 1L);

            assertThat(result).isNotNull();
            verify(streamRepository).save(any(Stream.class));
        }

        @Test
        @DisplayName("Lanza excepción si el usuario no existe")
        void createPost_userNotFound_throwsException() {
            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    postService.createPost("Hola mundo!", 99L)
            )
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("User not found");

            verify(postRepository, never()).save(any());
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Consultas
    // ─────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Consultas")
    class Queries {

        @Test
        @DisplayName("getAllPosts() retorna posts ordenados")
        void getAllPosts_returnsSortedPosts() {
            Post post2 = new Post("Segundo post", mockUser);
            post2.setId(2L);

            when(postRepository.findAllOrderByCreatedAtDesc())
                    .thenReturn(List.of(post2, mockPost));

            List<Post> result = postService.getAllPosts();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getId()).isEqualTo(2L);
        }

        @Test
        @DisplayName("getGlobalStreamPosts() retorna posts del stream global")
        void getGlobalStreamPosts_success() {
            when(streamRepository.findByName("global")).thenReturn(Optional.of(globalStream));
            when(postRepository.findByStreamIdOrderByCreatedAtDesc(1L))
                    .thenReturn(List.of(mockPost));

            List<Post> result = postService.getGlobalStreamPosts();

            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(mockPost);
        }

        @Test
        @DisplayName("getGlobalStreamPosts() lanza excepción si no existe stream global")
        void getGlobalStreamPosts_noGlobalStream_throws() {
            when(streamRepository.findByName("global")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> postService.getGlobalStreamPosts())
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Global stream not found");
        }

        @Test
        @DisplayName("getPostsByUserName() retorna posts del autor buscado")
        void getPostsByUserName_returnsMatchingPosts() {
            when(postRepository.findByUserNameContainingIgnoreCase("juan"))
                    .thenReturn(List.of(mockPost));

            List<Post> result = postService.getPostsByUserName("juan");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getUser().getName()).isEqualTo("Juan Pérez");
        }

        @Test
        @DisplayName("getPostsByUserName() retorna lista vacía si no hay coincidencias")
        void getPostsByUserName_noResults() {
            when(postRepository.findByUserNameContainingIgnoreCase("xyz"))
                    .thenReturn(List.of());

            assertThat(postService.getPostsByUserName("xyz")).isEmpty();
        }

        @Test
        @DisplayName("findById() retorna el post correcto")
        void findById_returnsPost() {
            when(postRepository.findById(1L)).thenReturn(Optional.of(mockPost));

            Optional<Post> result = postService.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getContent()).isEqualTo("Hola mundo!");
        }

        @Test
        @DisplayName("findById() retorna empty si no existe")
        void findById_notFound() {
            when(postRepository.findById(999L)).thenReturn(Optional.empty());

            assertThat(postService.findById(999L)).isEmpty();
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // Casos límite
    // ─────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Casos límite")
    class EdgeCases {

        @Test
        @DisplayName("Permite post con exactamente 140 caracteres")
        void createPost_maxLength_success() {
            String content140 = "A".repeat(140);
            Post longPost = new Post(content140, mockUser);
            longPost.setId(5L);
            longPost.setStream(globalStream);

            when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
            when(streamRepository.findByName("global")).thenReturn(Optional.of(globalStream));
            when(postRepository.save(any(Post.class))).thenReturn(longPost);

            Post result = postService.createPost(content140, 1L);
            assertThat(result.getContent()).hasSize(140);
        }
    }
}