package raul.Device_Management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import raul.Device_Management.entities.Device;
import raul.Device_Management.entities.User;
import raul.Device_Management.repositories.DeviceRepository;
import raul.Device_Management.repositories.UserRepository;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final DeviceRepository deviceRepository;

    public boolean exists(UUID userId) {
        return userRepository.existsById(userId);
    }

    public void create(UUID userId) {
        User user = new User();
        user.setId(userId);
        userRepository.save(user);
    }

    @Transactional
    public void delete(UUID userId) {
        List<Device> devices = deviceRepository.findByUserId(userId);
        devices.forEach(d -> d.setUserId(null));
        deviceRepository.saveAll(devices);
        userRepository.deleteById(userId);
    }
}
