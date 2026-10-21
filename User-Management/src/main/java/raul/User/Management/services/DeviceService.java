package raul.User.Management.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class DeviceService {
    private static final Logger log = LoggerFactory.getLogger(DeviceService.class);
    private final RestClient device;

    public DeviceService(@Value("${device.management.url}") String deviceManagementUrl) {
        this.device = RestClient.create(deviceManagementUrl);
    }

    public void createUser(UUID userId) {
        log.info("user-management -> device-management: create user {}", userId);
        device.post().uri("/internal/users/{id}", userId).retrieve().toBodilessEntity();
    }

    public void deleteUser(UUID userId) {
        log.info("user-management -> device-management: delete user {}", userId);
        device.delete().uri("/internal/users/{id}", userId).retrieve().toBodilessEntity();
    }
}
