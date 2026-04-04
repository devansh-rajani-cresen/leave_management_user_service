package com.cresensolutions.userservice.controller;

import com.cresensolutions.userservice.dto.DeleteUser;
import com.cresensolutions.userservice.dto.SuccessResponse;
import com.cresensolutions.userservice.dto.UserRequest;
import com.cresensolutions.userservice.dto.UserResponse;
import com.cresensolutions.userservice.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/get-users")
    public List<UserResponse> getAvailableUsers(){
        log.info("Fetching all users from DB!");
        return adminService.getUsers();
    }

    @PostMapping("/add-user")
    public ResponseEntity<SuccessResponse> addUser(@RequestBody UserRequest userRequest){
        log.info("Request from Frontend: {}", userRequest);
        log.info("Creating User in DB for: {}", userRequest.getUserName());
        adminService.saveUser(userRequest);
        return ResponseEntity.ok(
                new SuccessResponse("User created successfully")
        );
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<SuccessResponse> deleteUser(@RequestBody DeleteUser deleteUser){
        log.info("Deleting user with username: {}", deleteUser.getUserName());
        adminService.deleteUser(deleteUser);
        return ResponseEntity.ok(
                new SuccessResponse("User deleted successfully")
        );
    }

    @PutMapping("/update-user")
    public ResponseEntity<SuccessResponse> updateUser(@RequestBody UserRequest userRequest){
        log.info("Updating user for req: {}", userRequest);
        adminService.updateUser(userRequest);
        return ResponseEntity.ok(
                new SuccessResponse("User updated successfully")
        );
    }
}