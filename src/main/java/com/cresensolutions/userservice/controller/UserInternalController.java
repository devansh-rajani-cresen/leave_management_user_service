package com.cresensolutions.userservice.controller;

import com.cresensolutions.userservice.dto.BasicUserInfoForAI;
import com.cresensolutions.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/internal/users")
@RequiredArgsConstructor
@Slf4j
public class UserInternalController {

    private final UserService userService;

    // Total emp counts
    @GetMapping("/count")
    public Long getEmployeeCount(){
        log.info("Returning all emp counts");
        return userService.getEmployeeCount();
    }

    // Get users by role
    @GetMapping("/role/{role}")
    public List<BasicUserInfoForAI> getUserByRole(@PathVariable String role){
        log.info("Returning users with role: {}", role);
        return userService.getUserInfoByRole(role);
    }

    // Get basic user info
    @GetMapping("/search")
    public List<BasicUserInfoForAI> searchUsers(@RequestParam String name){
        log.info("Returning info of user : {}", name);
        return userService.searchUserByName(name);
    }
}
