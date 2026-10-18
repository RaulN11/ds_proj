package raul.Authentication.controllers;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import raul.Authentication.DTOs.UserDTO;
import raul.Authentication.repositories.UserCredentialsRepository;
import raul.Authentication.services.JWTService;
import raul.Authentication.services.UserService;
import raul.Authentication.entities.UserCredentials;
import raul.Authentication.DTOs.RegisterDTO;
import raul.Authentication.DTOs.LoginDTO;
import raul.Authentication.DTOs.JwtDTO;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final UserCredentialsRepository userCredentialsRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterDTO req) {
        if (req.getUsername() == null || req.getUsername().isBlank() || req.getPassword() == null || req.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body("username and password required");
        }
        if (userCredentialsRepository.findByUsername(req.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }

        UserCredentials c = new UserCredentials();
        c.setUsername(req.getUsername());
        c.setPasswordHash(passwordEncoder.encode(req.getPassword()));
        c.setRole("ADMIN".equalsIgnoreCase(req.getRole()) ? "ADMIN" : "CLIENT");
        userCredentialsRepository.save(c);

        try {
            UserDTO userDto = new UserDTO();
            userDto.setId(c.getId());
            userDto.setUsername(c.getUsername());
            userDto.setEmail(req.getEmail());
            userDto.setRole(c.getRole());
            userService.createUser(userDto);
        } catch (Exception e) {
            userCredentialsRepository.delete(c);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("user-service failed: " + e.getMessage());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("id", c.getId(), "username", c.getUsername(), "role", c.getRole()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO req) {
        return userCredentialsRepository.findByUsername(req.getUsername())
                .filter(c -> passwordEncoder.matches(req.getPassword(), c.getPasswordHash()))
                .<ResponseEntity<?>>map(c -> ResponseEntity.ok(new JwtDTO(
                        jwtService.generate(c), c.getRole(), c.getId().toString(), c.getUsername())))
                .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials"));
    }

    @DeleteMapping("/credentials/{id}")
    public ResponseEntity<Void> deleteCredentials(@PathVariable UUID id) {
        if (!userCredentialsRepository.existsById(id)) return ResponseEntity.notFound().build();
        userCredentialsRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    public ResponseEntity<Void> validate(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        if (auth == null || !auth.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Claims claims = jwtService.parse(auth.substring(7));
            return ResponseEntity.ok()
                    .header("X-User-Id", claims.get("id", String.class))
                    .header("X-User-Name", claims.getSubject())
                    .header("X-User-Role", claims.get("role", String.class))
                    .build();
        } catch (JwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @GetMapping("/validate-admin")
    public ResponseEntity<Void> validateAdmin(@RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String auth) {
        ResponseEntity<Void> r = validate(auth);
        if (!r.getStatusCode().is2xxSuccessful()) return r;
        return "ADMIN".equals(r.getHeaders().getFirst("X-User-Role"))
                ? r : ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }
}