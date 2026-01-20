package com.acme.platform.api;

import com.acme.platform.model.User;
import com.acme.platform.service.UserSpecificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/spec/users")
public class UserSpecificationController {
    
    private final UserSpecificationService userSpecificationService;
    
    public UserSpecificationController(UserSpecificationService userSpecificationService) {
        this.userSpecificationService = userSpecificationService;
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<User>> searchUsers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String address) {
        List<User> users = userSpecificationService.searchUsers(name, email, address);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/with-address")
    public ResponseEntity<List<User>> getUsersWithAddress() {
        List<User> users = userSpecificationService.findUsersWithAddress();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/without-address")
    public ResponseEntity<List<User>> getUsersWithoutAddress() {
        List<User> users = userSpecificationService.findUsersWithoutAddress();
        return ResponseEntity.ok(users);
    }
}
