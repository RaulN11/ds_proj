package raul.User.Management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import raul.User.Management.entities.User;
import raul.User.Management.repositories.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public Boolean exists(String username){
        return userRepository.findByUsername(username) != null;
    }

    public User createUser(User user){
        return userRepository.save(user);
    }
    public User findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUser(UUID id, User userDetails) {
        User user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setUsername(userDetails.getUsername());
            user.setPassword(userDetails.getPassword());
            user.setEmail(userDetails.getEmail());
            user.setRole(userDetails.getRole());
            return userRepository.save(user);
        }
        return null;
    }

    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
}
