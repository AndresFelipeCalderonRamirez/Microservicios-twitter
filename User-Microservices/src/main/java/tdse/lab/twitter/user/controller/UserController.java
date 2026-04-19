package tdse.lab.twitter.user.controller;


import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tdse.lab.twitter.user.model.User;
import tdse.lab.twitter.user.service.UserService;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            User user = userService.createUser(request.getName(), request.getEmail(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = userService.authenticate(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.findAll());
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchByName(@RequestParam String name) {
        List<User> users = userService.findByName(name);
        if (users.isEmpty()) return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "No users found with name: " + name));
        return ResponseEntity.ok(users);
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getMe(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("https://twtr-api/email");
        String name  = jwt.getClaimAsString("https://twtr-api/name");
        String sub   = jwt.getSubject();

        if (email == null || email.isBlank()) email = sub + "@auth0.com";
        if (name  == null || name.isBlank())  name  = email.split("@")[0];

        final String fe = email, fn = name;
        User user = userService.findByEmail(email)
                .orElseGet(() -> userService.createUser(fn, fe, sub));

        return ResponseEntity.ok(Map.of(
                "id",    user.getId(),
                "name",  user.getName(),
                "email", user.getEmail(),
                "sub",   sub
        ));
    }

    public static class RegisterRequest {
        private String name, email, password;
        public String getName() { return name; }
        public void setName(String n) { this.name = n; }
        public String getEmail() { return email; }
        public void setEmail(String e) { this.email = e; }
        public String getPassword() { return password; }
        public void setPassword(String p) { this.password = p; }
    }

    public static class LoginRequest {
        private String email, password;
        public String getEmail() { return email; }
        public void setEmail(String e) { this.email = e; }
        public String getPassword() { return password; }
        public void setPassword(String p) { this.password = p; }
    }
}