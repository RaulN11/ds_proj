package raul.Device_Management.controllers;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import raul.Device_Management.entities.Device;
import raul.Device_Management.services.DeviceService;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
@RequiredArgsConstructor
public class DeviceController {
    private final DeviceService deviceService;

    @GetMapping
    public List<Device> getAllDevices() { return deviceService.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable UUID id) {
        Device device = deviceService.findById(id);
        return device != null ? ResponseEntity.ok(device) : ResponseEntity.notFound().build();
    }

    @GetMapping("/user/{userId}")
    public List<Device> getDevicesByUserId(@PathVariable UUID userId) {
        return deviceService.findByUserId(userId);
    }

    @PostMapping
    public ResponseEntity<Device> addDevice(@RequestBody Device device) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.createDevice(device));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable UUID id, @RequestBody Device device) {
        Device updatedDevice = deviceService.updateDevice(id, device);
        return updatedDevice != null ? ResponseEntity.ok(updatedDevice) : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }
}