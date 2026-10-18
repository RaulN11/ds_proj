package raul.Authentication.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;
@Data
@Entity
public class UserCredentials {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role;
}
