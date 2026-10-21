package raul.Device_Management.entities;

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
}
