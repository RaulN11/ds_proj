package raul.User.Management.services;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DeviceService {
    private final RestClient device;

    public DeviceService(){
        String deviceManagementURL = "${device.service.url}";
        this.device = RestClient.create(deviceManagementURL);

    }
}
