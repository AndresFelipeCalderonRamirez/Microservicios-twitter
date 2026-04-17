package tdse.lab.twitter.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.web.servlet.MockMvc;
import tdse.lab.twitter.config.SecurityConfig;
import tdse.lab.twitter.model.Stream;
import tdse.lab.twitter.service.StreamService;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// ══════════════════════════════════════════════════════════════════════════
// STREAM CONTROLLER
// ══════════════════════════════════════════════════════════════════════════
@WebMvcTest(StreamController.class)
@Import(SecurityConfig.class)
@DisplayName("StreamController — Pruebas de Integración")
class StreamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StreamService streamService;
    @MockBean
    private JwtDecoder jwtDecoder;

    private Stream globalStream;

    @BeforeEach
    void setUp() {
        globalStream = new Stream("global");
        globalStream.setId(1L);
    }

    @Test
    @DisplayName("GET /api/streams — 200 sin token (público)")
    void getAllStreams_public_returns200() throws Exception {
        when(streamService.findAll()).thenReturn(List.of(globalStream));

        mockMvc.perform(get("/api/streams"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("global"));
    }

    @Test
    @DisplayName("GET /api/streams/global — 200 sin token (público)")
    void getGlobalStream_public_returns200() throws Exception {
        when(streamService.getGlobalStream()).thenReturn(globalStream);

        mockMvc.perform(get("/api/streams/global"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("global"));
    }

    @Test
    @DisplayName("GET /api/streams/{id} — 200 stream encontrado")
    void getStreamById_found_returns200() throws Exception {
        when(streamService.findById(1L)).thenReturn(Optional.of(globalStream));

        mockMvc.perform(get("/api/streams/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/streams/{id} — 404 stream no encontrado")
    void getStreamById_notFound_returns404() throws Exception {
        when(streamService.findById(999L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/streams/999"))
                .andExpect(status().isNotFound());
    }
}
