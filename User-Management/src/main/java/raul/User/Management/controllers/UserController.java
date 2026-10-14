package raul.User.Management.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import raul.User.Management.entities.User;
import raul.User.Management.services.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> getAllUsers(){
        return userService.findAll();
    }

    @PostMapping
    public ResponseEntity<?> addUser(@RequestBody User user){
        if(userService.exists(user.getUsername())){
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username already exists");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(user));
    }

}
