package raul.Device_Management.controllers;

import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "List all devices")
    @GetMapping
    public List<Device> getAllDevices() { return deviceService.findAll(); }

    @Operation(summary = "Get a device by id")
    @GetMapping("/{id}")
    public ResponseEntity<Device> getDeviceById(@PathVariable UUID id) {
        Device device = deviceService.findById(id);
        return device != null ? ResponseEntity.ok(device) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "List the devices assigned to a user")
    @GetMapping("/user/{userId}")
    public List<Device> getDevicesByUserId(@PathVariable UUID userId) {
        return deviceService.findByUserId(userId);
    }

    @Operation(summary = "Create a device (unassigned)")
    @PostMapping
    public ResponseEntity<Device> addDevice(@RequestBody Device device) {
        return ResponseEntity.status(HttpStatus.CREATED).body(deviceService.createDevice(device));
    }

    @Operation(summary = "Update name, description and max consumption")
    @PutMapping("/{id}")
    public ResponseEntity<Device> updateDevice(@PathVariable UUID id, @RequestBody Device device) {
        Device updatedDevice = deviceService.updateDevice(id, device);
        return updatedDevice != null ? ResponseEntity.ok(updatedDevice) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Delete a device")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Assign a device to a user (the user is verified in user-management)")
    @PutMapping("/{id}/assign/{userId}")
    public ResponseEntity<?> assign(@PathVariable UUID id, @PathVariable UUID userId) {
        try {
            Device d = deviceService.assign(id, userId);
            return d != null ? ResponseEntity.ok(d) : ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @Operation(summary = "Unassign a device")
    @PutMapping("/{id}/unassign")
    public ResponseEntity<Device> unassign(@PathVariable UUID id) {
        Device d = deviceService.unassign(id);
        return d != null ? ResponseEntity.ok(d) : ResponseEntity.notFound().build();
    }

    @Operation(summary = "Unassign all devices of a user (called by user-management on user delete)")
    @PutMapping("/user/{userId}/unassign")
    public ResponseEntity<Void> unassignUser(@PathVariable UUID userId) {
        deviceService.unassignAllOf(userId);
        return ResponseEntity.noContent().build();
    }
}
