package tdse.lab.twitter.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import tdse.lab.twitter.model.Stream;
import tdse.lab.twitter.repository.StreamRepository;
import java.util.List;
import java.util.Optional;

@Service
public class StreamService {

    private final StreamRepository streamRepository;

    @PostConstruct
    public void init() {
        if (streamRepository.findByName("global").isEmpty()) {
            Stream global = new Stream();
            global.setName("global");
            streamRepository.save(global);
        }
    }

    public StreamService(StreamRepository streamRepository) {
        this.streamRepository = streamRepository;
    }

    public Stream getGlobalStream() {
        return streamRepository.findByName("global")
                .orElseGet(() -> streamRepository.save(new Stream("global")));
    }

    public Optional<Stream> findById(Long id) {
        return streamRepository.findById(id);
    }

    public List<Stream> findAll() {
        return streamRepository.findAll();
    }
}