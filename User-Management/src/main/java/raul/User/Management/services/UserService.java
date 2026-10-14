package raul.User.Management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import raul.User.Management.entities.User;
import raul.User.Management.repositories.UserRepository;

import java.util.List;

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
}
