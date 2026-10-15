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

    public List<Device> findAll() { return deviceRepository.findAll(); }

    public Device findById(UUID id) { return deviceRepository.findById(id).orElse(null); }

    public List<Device> findByUserId(UUID userId) { return deviceRepository.findByUserId(userId); }

    public Device createDevice(Device device) { return deviceRepository.save(device); }

    public Device updateDevice(UUID id, Device deviceDetails) {
        Device device = findById(id);
        if (device != null) {
            device.setName(deviceDetails.getName());
            device.setMaxConsumption(deviceDetails.getMaxConsumption());
            device.setUserId(deviceDetails.getUserId());
            return deviceRepository.save(device);
        }
        return null;
    }

    public void deleteDevice(UUID id) { deviceRepository.deleteById(id); }
}