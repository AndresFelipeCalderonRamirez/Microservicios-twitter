package tdse.lab.twitter.posts.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Content is required")
    @Size(max = 140, message = "Post content cannot exceed 140 characters")
    @Column(nullable = false, length = 140)
    private String content;

    // Solo guardamos el ID — no referenciamos el User Service directamente
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Guardamos el nombre al crear el post para no depender del User Service al leer
    @Column(name = "user_name")
    private String userName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public Post() {}

    public Post(String content, Long userId, String userName) {
        this.content   = content;
        this.userId    = userId;
        this.userName  = userName;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}