package tdse.lab.twitter.posts.service;

import org.springframework.stereotype.Service;
import tdse.lab.twitter.posts.model.Post;
import tdse.lab.twitter.posts.repository.PostRepository;
import java.util.List;
import java.util.Optional;

@Service
public class PostService {

    private final PostRepository postRepository;

    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Post createPost(String content, Long userId, String userName) {
        return postRepository.save(new Post(content, userId, userName));
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllOrderByCreatedAtDesc();
    }

    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Post> getPostsByUserName(String name) {
        return postRepository.findByUserNameContainingIgnoreCase(name);
    }

    public List<Post> searchByContent(String content) {
        return postRepository.findByContentContainingIgnoreCase(content);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }
}