package tdse.lab.twitter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tdse.lab.twitter.model.User;
import tdse.lab.twitter.service.UserService;

import java.util.Map;

@RestController
@RequestMapping("/api/me")
@Tag(name = "Me", description = "Perfil del usuario autenticado")
@SecurityRequirement(name = "Bearer Token")
public class MeController {

    private final UserService userService;

    public MeController(UserService userService) {
        this.userService = userService;
    }

    @Operation(
            summary = "Obtener o crear perfil",
            description = "Retorna el perfil del usuario autenticado. " +
                    "Si es la primera vez que inicia sesión con Auth0, " +
                    "lo registra automáticamente en el sistema."
    )
    @ApiResponse(responseCode = "200", description = "Perfil obtenido o creado exitosamente")
    @ApiResponse(responseCode = "401", description = "Token inválido o ausente")
    @GetMapping
    public ResponseEntity<Map<String, Object>> getMe(@AuthenticationPrincipal Jwt jwt) {

        String email = jwt.getClaimAsString("https://twtr-api/email");
        String name  = jwt.getClaimAsString("https://twtr-api/name");
        String sub   = jwt.getSubject();

        // Si no tiene email en el token usar el sub como fallback
        if (email == null || email.isBlank()) {
            email = sub + "@auth0.com";
        }
        if (name == null || name.isBlank()) {
            name = email.split("@")[0];
        }

        // Auto-provisioning: buscar o crear el usuario en la base de datos
        final String finalEmail = email;
        final String finalName  = name;

        User user = userService.findByEmail(email).orElseGet(() ->
                userService.createUser(finalName, finalEmail, sub) // usa el sub como contraseña interna
        );

        Map<String, Object> profile = Map.of(
                "sub",      sub,
                "email",    user.getEmail(),
                "name",     user.getName(),
                "id",       user.getId(),
                "scopes",   jwt.getClaimAsString("scope") != null ? jwt.getClaimAsString("scope") : ""
        );

        return ResponseEntity.ok(profile);
    }
}