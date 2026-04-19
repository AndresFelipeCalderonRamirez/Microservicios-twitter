package tdse.lab.twitter.posts.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tdse.lab.twitter.posts.model.Post;
import tdse.lab.twitter.posts.service.PostService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Posts Service", description = "Microservicio de posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @Operation(summary = "Crear post")
    @SecurityRequirement(name = "Bearer Token")
    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody CreatePostRequest request) {
        try {
            Post post = postService.createPost(
                    request.getContent(), request.getUserId(), request.getUserName());
            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(summary = "Obtener todos los posts")
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @Operation(summary = "Stream global")
    @GetMapping("/stream")
    public ResponseEntity<List<Post>> getStream() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @Operation(summary = "Posts por nombre de usuario")
    @GetMapping("/user")
    public ResponseEntity<?> getByUserName(@RequestParam String name) {
        List<Post> posts = postService.getPostsByUserName(name);
        if (posts.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No posts found for user: " + name));
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Buscar por contenido")
    @GetMapping("/search")
    public ResponseEntity<?> searchByContent(@RequestParam String content) {
        List<Post> posts = postService.searchByContent(content);
        if (posts.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No posts found with: " + content));
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Post por ID")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return postService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    public static class CreatePostRequest {
        @NotBlank(message = "Content is required")
        private String content;

        @NotNull(message = "userId is required")
        private Long userId;

        @NotBlank(message = "userName is required")
        private String userName;

        public String getContent() { return content; }
        public void setContent(String c) { this.content = c; }
        public Long getUserId() { return userId; }
        public void setUserId(Long u) { this.userId = u; }
        public String getUserName() { return userName; }
        public void setUserName(String u) { this.userName = u; }
    }
}