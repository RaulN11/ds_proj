package raul.Authentication.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import raul.Authentication.entities.UserCredentials;

import java.util.Optional;
import java.util.UUID;

public interface UserCredentialsRepository extends JpaRepository<UserCredentials, UUID> {
    Optional<UserCredentials> findByUsername(String username);
}
