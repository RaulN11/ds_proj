package raul.Device_Management.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import raul.Device_Management.entities.Device;

import java.util.List;
import java.util.UUID;

public interface DeviceRepository extends JpaRepository<Device, UUID> {
    List<Device> findByUserId(UUID userId);
}
