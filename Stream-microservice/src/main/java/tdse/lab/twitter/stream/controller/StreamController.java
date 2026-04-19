package tdse.lab.twitter.stream.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@RestController
@RequestMapping("/api/streams")
@Tag(name = "Stream Service", description = "Microservicio de stream/feed global")
public class StreamController {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${posts.service.url}")
    private String postsServiceUrl;

    // GET /api/streams/global
    @Operation(summary = "Stream global", description = "Retorna el feed global de posts. Público.")
    @GetMapping("/global")
    public ResponseEntity<?> getGlobalStream() {
        try {
            Object posts = restTemplate.getForObject(
                    postsServiceUrl + "/api/posts/stream", Object.class);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Posts service unavailable: " + e.getMessage()));
        }
    }

    // GET /api/streams/user?name=Juan
    @Operation(summary = "Stream por usuario", description = "Posts de un usuario específico. Público.")
    @GetMapping("/user")
    public ResponseEntity<?> getStreamByUser(@RequestParam String name) {
        try {
            Object posts = restTemplate.getForObject(
                    postsServiceUrl + "/api/posts/user?name=" + name, Object.class);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Posts service unavailable: " + e.getMessage()));
        }
    }

    // GET /api/streams/search?content=hola
    @Operation(summary = "Buscar en el stream", description = "Busca posts por contenido. Público.")
    @GetMapping("/search")
    public ResponseEntity<?> searchStream(@RequestParam String content) {
        try {
            Object posts = restTemplate.getForObject(
                    postsServiceUrl + "/api/posts/search?content=" + content, Object.class);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Posts service unavailable: " + e.getMessage()));
        }
    }
}