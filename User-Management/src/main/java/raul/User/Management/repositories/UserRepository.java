package raul.User.Management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import raul.User.Management.entities.User;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    User findByUsername(String username);
}
