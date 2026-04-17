package tdse.lab.twitter.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public  class LoginRequest {
    @Schema(example = "juan@gmail.com")
    private String email;

    @Schema(example = "123456")
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}