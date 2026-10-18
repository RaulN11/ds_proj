package raul.Device_Management.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import raul.Device_Management.entities.Device;
import raul.Device_Management.repositories.DeviceRepository;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {
    private final DeviceRepository deviceRepository;
    private final UserService userService;

    public List<Device> findAll() { return deviceRepository.findAll(); }

    public Device findById(UUID id) { return deviceRepository.findById(id).orElse(null); }

    public List<Device> findByUserId(UUID userId) { return deviceRepository.findByUserId(userId); }

    public Device createDevice(Device device) {
        device.setId(null);
        device.setUserId(null); // assignment goes through assign()
        return deviceRepository.save(device);
    }

    public Device updateDevice(UUID id, Device details) {
        Device device = findById(id);
        if (device == null) return null;
        device.setName(details.getName());
        device.setDescription(details.getDescription());
        device.setMaxConsumption(details.getMaxConsumption());
        return deviceRepository.save(device);
    }

    public void deleteDevice(UUID id) { deviceRepository.deleteById(id); }

    public Device assign(UUID deviceId, UUID userId) {
        Device device = findById(deviceId);
        if (device == null) return null;
        if (!userService.exists(userId)) throw new IllegalArgumentException("User not found in user-management");
        device.setUserId(userId);
        return deviceRepository.save(device);
    }

    public Device unassign(UUID deviceId) {
        Device device = findById(deviceId);
        if (device == null) return null;
        device.setUserId(null);
        return deviceRepository.save(device);
    }

    public void unassignAllOf(UUID userId) {
        List<Device> devices = deviceRepository.findByUserId(userId);
        devices.forEach(d -> d.setUserId(null));
        deviceRepository.saveAll(devices);
    }
}
