package com.cresensolutions.userservice.controller;

import com.cresensolutions.userservice.dto.DeleteUser;
import com.cresensolutions.userservice.dto.SuccessResponse;
import com.cresensolutions.userservice.dto.UserRequest;
import com.cresensolutions.userservice.dto.UserResponse;
import com.cresensolutions.userservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/admin")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/get-users")
    public List<UserResponse> getAvailableUsers(){
        return adminService.getUsers();
    }

    @PostMapping("/add-user")
    public ResponseEntity<SuccessResponse> addUser(@RequestBody UserRequest userRequest){
        adminService.saveUser(userRequest);
        return ResponseEntity.ok(
                new SuccessResponse("User created!")
        );
    }

    @DeleteMapping("/delete-user")
    public ResponseEntity<SuccessResponse> deleteUser(@RequestBody DeleteUser deleteUser){
        adminService.deleteUser(deleteUser);
        return ResponseEntity.ok(
                new SuccessResponse("User deleted!")
        );
    }

    @PutMapping("/update-user")
    public ResponseEntity<SuccessResponse> updateUser(@RequestBody UserRequest userRequest){
        adminService.updateUser(userRequest);
        return ResponseEntity.ok(
                new SuccessResponse("User updated!")
        );
    }
}