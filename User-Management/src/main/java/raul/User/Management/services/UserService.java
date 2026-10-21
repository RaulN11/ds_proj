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
    private final DeviceService deviceService;
    private final AuthService authService;

    public List<User> findAll(){
        return userRepository.findAll();
    }

    public Boolean exists(String username){
        return userRepository.findByUsername(username) != null;
    }

    public User createUser(User user){
        User saved = userRepository.save(user);
        try {
            deviceService.createUser(saved.getId());
        } catch (RuntimeException e) {
            userRepository.delete(saved);
            throw e;
        }
        return saved;
    }
    public User findById(UUID id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateUser(UUID id, User details) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) return null;
        user.setName(details.getName());
        user.setEmail(details.getEmail());
        user.setAge(details.getAge());
        return userRepository.save(user);
    }

    public boolean deleteUser(UUID id) {
        if (!userRepository.existsById(id)) return false;
        userRepository.deleteById(id);
        try { deviceService.deleteUser(id); } catch (Exception e){
            System.out.println(e.getMessage());
        }
        try { authService.deleteCredentials(id); } catch (Exception e){
            System.out.println(e.getMessage());
        }
        return true;
    }
}
