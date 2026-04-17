package tdse.lab.twitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tdse.lab.twitter.model.Post;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p ORDER BY p.createdAt DESC")
    List<Post> findAllOrderByCreatedAtDesc();

    @Query("SELECT p FROM Post p WHERE p.stream.id = :streamId ORDER BY p.createdAt DESC")
    List<Post> findByStreamIdOrderByCreatedAtDesc(@Param("streamId") Long streamId);

    List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
    @Query("SELECT p FROM Post p WHERE LOWER(p.user.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY p.createdAt DESC")
    List<Post> findByUserNameContainingIgnoreCase(@Param("name") String name);
}