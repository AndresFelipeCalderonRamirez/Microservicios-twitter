package tdse.lab.twitter.service;

import org.springframework.stereotype.Service;
import tdse.lab.twitter.model.Post;
import tdse.lab.twitter.model.Stream;
import tdse.lab.twitter.model.User;
import tdse.lab.twitter.repository.PostRepository;
import tdse.lab.twitter.repository.StreamRepository;
import tdse.lab.twitter.repository.UserRepository;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final StreamRepository streamRepository;
    private final UserRepository userRepository;

    public PostService(PostRepository postRepository,
                       StreamRepository streamRepository,
                       UserRepository userRepository) {
        this.postRepository = postRepository;
        this.streamRepository = streamRepository;
        this.userRepository = userRepository;
    }

    public Post createPost(String content, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Stream globalStream = streamRepository.findByName("global")
                .orElseGet(() -> streamRepository.save(new Stream("global")));

        Post post = new Post(content, user);
        post.setStream(globalStream);

        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllOrderByCreatedAtDesc();
    }

    public List<Post> getGlobalStreamPosts() {
        Stream globalStream = streamRepository.findByName("global")
                .orElseGet(() -> streamRepository.save(new Stream("global")));

        return postRepository.findByStreamIdOrderByCreatedAtDesc(globalStream.getId());
    }

    public List<Post> getPostsByUser(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }
    public List<Post> getPostsByUserName(String name) {
        return postRepository.findByUserNameContainingIgnoreCase(name);
    }
}