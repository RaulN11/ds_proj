package raul.Authentication.services;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import raul.Authentication.DTOs.UserDTO;

@Service
public class UserService {
    private final RestClient users;
    public UserService(){
        String userManagementUrl="${user.management.url}";
        this.users = RestClient.create(userManagementUrl);
    }
    public void createUser(UserDTO user){
        users.post().uri("/users").contentType(MediaType.APPLICATION_JSON).body(user).retrieve().toBodilessEntity();
    }
}
