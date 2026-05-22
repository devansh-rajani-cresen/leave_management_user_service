package com.cresensolutions.userservice.controller;

import com.cresensolutions.userservice.entity.UserAchievement;
import com.cresensolutions.userservice.service.AchievementService;
import com.cresensolutions.userservice.util.JwtRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/achievements")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;
    private final JwtRequestUtil jwtRequestUtil;

    // Get My Achievements
    @GetMapping("/my-achievements")
    public ResponseEntity<List<UserAchievement>> getMyAchievements(HttpServletRequest request) {
        String token = jwtRequestUtil.extractToken(request);
        return ResponseEntity.ok(
                achievementService.getMyAchievements(token)
        );
    }

    // Upload Achievements
    @PostMapping("/upload-achievement")
    public ResponseEntity<String> uploadAchievement(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam("shortDescriptions") List<String> shortDescriptions,
            HttpServletRequest request
    ) {
        String token = jwtRequestUtil.extractToken(request);
        String response = achievementService.uploadAchievement(files, shortDescriptions, token);
        return ResponseEntity.ok(response);
    }

    // Delete Achievement
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAchievement(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String token = jwtRequestUtil.extractToken(request);
        return ResponseEntity.ok(
                achievementService.deleteAchievement(id, token)
        );
    }

    // View Achievement
    @GetMapping("/view/{id}")
    public ResponseEntity<String> viewAchievement(
            @PathVariable Long id,
            HttpServletRequest request
    ) {
        String token = jwtRequestUtil.extractToken(request);
        return ResponseEntity.ok(
                achievementService.getFileUrl(id, token)
        );
    }

    // Get All Achievements
    @GetMapping("/all-achievements")
    public ResponseEntity<List<UserAchievement>> getAllAchievements() {
        return ResponseEntity.ok(
                achievementService.getAllAchievements()
        );
    }
}
