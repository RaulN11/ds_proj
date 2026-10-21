package raul.Device_Management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import raul.Device_Management.entities.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
