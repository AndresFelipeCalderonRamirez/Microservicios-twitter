package tdse.lab.twitter.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tdse.lab.twitter.model.Stream;
import tdse.lab.twitter.repository.StreamRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StreamService — Pruebas Unitarias")
class StreamServiceTest {

    @Mock
    private StreamRepository streamRepository;

    @InjectMocks
    private StreamService streamService;

    private Stream globalStream;

    @BeforeEach
    void setUp() {
        globalStream = new Stream("global");
        globalStream.setId(1L);
    }

    @Nested
    @DisplayName("getGlobalStream()")
    class GetGlobalStream {

        @Test
        @DisplayName("Retorna el stream global existente")
        void getGlobalStream_exists() {
            when(streamRepository.findByName("global")).thenReturn(Optional.of(globalStream));

            Stream result = streamService.getGlobalStream();

            assertThat(result.getName()).isEqualTo("global");
            assertThat(result.getId()).isEqualTo(1L);
            verify(streamRepository, never()).save(any());
        }

        @Test
        @DisplayName("Crea y persiste el stream global si no existe")
        void getGlobalStream_createsIfMissing() {
            when(streamRepository.findByName("global")).thenReturn(Optional.empty());
            when(streamRepository.save(any(Stream.class))).thenReturn(globalStream);

            Stream result = streamService.getGlobalStream();

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("global");
            verify(streamRepository).save(any(Stream.class));
        }
    }

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("Retorna stream cuando existe el ID")
        void findById_found() {
            when(streamRepository.findById(1L)).thenReturn(Optional.of(globalStream));

            Optional<Stream> result = streamService.findById(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Retorna empty cuando el ID no existe")
        void findById_notFound() {
            when(streamRepository.findById(999L)).thenReturn(Optional.empty());

            assertThat(streamService.findById(999L)).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("Retorna todos los streams")
        void findAll_returnsAllStreams() {
            Stream tech = new Stream("tech");
            tech.setId(2L);

            when(streamRepository.findAll()).thenReturn(List.of(globalStream, tech));

            List<Stream> result = streamService.findAll();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(Stream::getName)
                    .containsExactly("global", "tech");
        }

        @Test
        @DisplayName("Retorna lista vacía cuando no hay streams")
        void findAll_empty() {
            when(streamRepository.findAll()).thenReturn(List.of());

            assertThat(streamService.findAll()).isEmpty();
        }
    }
}