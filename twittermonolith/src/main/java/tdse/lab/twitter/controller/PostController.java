package tdse.lab.twitter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tdse.lab.twitter.dto.CreatePostRequest;
import tdse.lab.twitter.model.Post;
import tdse.lab.twitter.service.PostService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
@Tag(name = "Posts", description = "Operaciones relacionadas con posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    // ── PROTEGIDO: requiere JWT ──────────────────────────────────────────
    @Operation(summary = "Crear un post", description = "Crea un nuevo post. Requiere autenticación (scope: write:posts)")
    @SecurityRequirement(name = "Bearer Token")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Post creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Contenido inválido o usuario no encontrado"),
            @ApiResponse(responseCode = "401", description = "Token inválido o ausente")
    })
    @PostMapping
    public ResponseEntity<?> createPost(@Valid @RequestBody CreatePostRequest request) {
        try {
            Post post = postService.createPost(request.getContent(), request.getUserId());
            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // ── PÚBLICOS: no requieren JWT ───────────────────────────────────────
    @Operation(summary = "Obtener todos los posts", description = "Retorna todos los posts. Público.")
    @ApiResponse(responseCode = "200", description = "Lista de posts obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<Post>> getAllPosts() {
        return ResponseEntity.ok(postService.getAllPosts());
    }

    @Operation(summary = "Stream global", description = "Retorna los posts del stream global. Público.")
    @ApiResponse(responseCode = "200", description = "Posts del stream global")
    @GetMapping("/stream")
    public ResponseEntity<List<Post>> getGlobalStream() {
        return ResponseEntity.ok(postService.getGlobalStreamPosts());
    }

    @Operation(summary = "Posts por nombre de usuario", description = "Busca posts por nombre del autor. Público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Posts encontrados"),
            @ApiResponse(responseCode = "404", description = "No se encontraron posts")
    })
    @GetMapping("/user")
    public ResponseEntity<?> getPostsByUserName(
            @Parameter(description = "Nombre del autor", example = "Juan")
            @RequestParam String name) {
        List<Post> posts = postService.getPostsByUserName(name);
        if (posts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "No posts found for user: " + name));
        }
        return ResponseEntity.ok(posts);
    }

    @Operation(summary = "Post por ID", description = "Retorna un post por su ID. Público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Post encontrado"),
            @ApiResponse(responseCode = "404", description = "Post no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(
            @Parameter(description = "ID del post", example = "1")
            @PathVariable Long id) {
        return postService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}