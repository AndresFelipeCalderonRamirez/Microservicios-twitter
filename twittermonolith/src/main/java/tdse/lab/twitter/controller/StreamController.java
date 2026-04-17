package tdse.lab.twitter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tdse.lab.twitter.model.Stream;
import tdse.lab.twitter.service.StreamService;
import java.util.List;

@RestController
@RequestMapping("/api/streams")
@Tag(name = "Streams", description = "Operaciones relacionadas con streams (público)")
public class StreamController {

    private final StreamService streamService;

    public StreamController(StreamService streamService) {
        this.streamService = streamService;
    }

    @Operation(summary = "Obtener todos los streams", description = "Retorna todos los streams. Público.")
    @ApiResponse(responseCode = "200", description = "Lista de streams")
    @GetMapping
    public ResponseEntity<List<Stream>> getAllStreams() {
        return ResponseEntity.ok(streamService.findAll());
    }

    @Operation(summary = "Stream global", description = "Retorna el stream global. Público.")
    @ApiResponse(responseCode = "200", description = "Stream global obtenido")
    @GetMapping("/global")
    public ResponseEntity<Stream> getGlobalStream() {
        return ResponseEntity.ok(streamService.getGlobalStream());
    }

    @Operation(summary = "Stream por ID", description = "Retorna un stream por ID. Público.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Stream encontrado"),
            @ApiResponse(responseCode = "404", description = "Stream no encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getStreamById(
            @Parameter(description = "ID del stream", example = "1")
            @PathVariable Long id) {
        return streamService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}