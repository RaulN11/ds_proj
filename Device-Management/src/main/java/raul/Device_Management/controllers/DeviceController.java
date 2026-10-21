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

    private static boolean isAdmin(String role) { return "ADMIN".equals(role); }

    @GetMapping
    public ResponseEntity<List<Device>> getAllDevices(
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader("X-User-Role") String role) {
        if (isAdmin(role)) return ResponseEntity.ok(deviceService.findAll());
        try {
            return ResponseEntity.ok(deviceService.findByUserId(UUID.fromString(userIdHeader)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader("X-User-Role") String role) {
        Device device = deviceService.findById(id);
        if (device == null) return ResponseEntity.notFound().build();
        if (isAdmin(role)) return ResponseEntity.ok(device);
        try {
            if (!UUID.fromString(userIdHeader).equals(device.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(device);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Device>> getDevicesByUserId(
            @PathVariable UUID userId,
            @RequestHeader("X-User-Id") String userIdHeader,
            @RequestHeader("X-User-Role") String role) {
        if (!isAdmin(role)) {
            try {
                if (!userId.equals(UUID.fromString(userIdHeader))) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(deviceService.findByUserId(userId));
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

    @PutMapping("/{id}/assign/{userId}")
    public ResponseEntity<?> assign(@PathVariable UUID id, @PathVariable UUID userId) {
        try {
            Device d = deviceService.assign(id, userId);
            return d != null ? ResponseEntity.ok(d) : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/unassign")
    public ResponseEntity<Device> unassign(@PathVariable UUID id) {
        Device d = deviceService.unassign(id);
        return d != null ? ResponseEntity.ok(d) : ResponseEntity.notFound().build();
    }
}
