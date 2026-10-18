package raul.User.Management.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.UUID;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    private final RestClient auth;

    public AuthService(@Value("${auth.url}") String authUrl) {
        this.auth = RestClient.create(authUrl);
    }

    public void deleteCredentials(UUID userId) {
        log.info("user-management -> auth: DELETE /auth/credentials/{}", userId);
        auth.delete().uri("/auth/credentials/{id}", userId).retrieve().toBodilessEntity();
    }
}
