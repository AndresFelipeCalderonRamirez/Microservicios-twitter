package tdse.lab.twitter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import tdse.lab.twitter.model.User;

public class CreatePostRequest {
    @Schema(example = "Este es mi primer post.")
    @NotBlank(message = "Content is required")
    private String content;

    @Schema(example = "1")
    @NotNull(message = "userId is required")
    private Long userId;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
