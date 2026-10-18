package raul.Device_Management.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final RestClient users;

    public UserService(@Value("${user.management.url}") String url) {
        this.users = RestClient.create(url);
    }

    public boolean exists(UUID userId) {
        log.info("device-management -> user-management: GET /users/{}", userId);
        try {
            users.get().uri("/users/{id}", userId).retrieve().toBodilessEntity();
            return true;
        } catch (HttpClientErrorException.NotFound e) {
            return false;
        }
    }
}
