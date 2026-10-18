package raul.User.Management.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "usersdb")
@Getter
@Setter
public class User {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true)
    private String username;
    private String name;
    private String email;
    private Integer age;
    private String role = "CLIENT";


}
