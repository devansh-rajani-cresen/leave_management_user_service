package com.cresensolutions.userservice.controller;

import com.cresensolutions.userservice.dto.UserRequest;
import com.cresensolutions.userservice.dto.UserResponse;
import com.cresensolutions.userservice.service.AdminService;
import lombok.extern.slf4j.Slf4j;
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
    public boolean addUser(@RequestBody UserRequest userRequest){
        log.info("Request from Frontend: {}", userRequest);
        log.info("Creating User in DB for: {}", userRequest.getUserName());
        return adminService.saveUser(userRequest);
    }

    @DeleteMapping("/delete-user/{username}")
    public boolean deleteUser(@PathVariable String username){
        log.info("Deleting user with username: {}", username);
        return adminService.deleteUserByUsername(username);
    }

    @PutMapping("/update-user")
    public boolean updateUser(@RequestBody UserRequest userRequest){
        log.info("Updating user for req: {}", userRequest);
        return adminService.updateUser(userRequest);
    }
}
