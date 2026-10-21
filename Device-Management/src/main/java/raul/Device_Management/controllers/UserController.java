package raul.Device_Management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import raul.Device_Management.services.UserService;

import java.util.UUID;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/{id}")
    public ResponseEntity<Void> create(@PathVariable UUID id) {
        userService.create(id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
