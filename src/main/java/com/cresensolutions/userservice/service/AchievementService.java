package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.entity.UserAchievement;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface AchievementService {
    List<UserAchievement> getMyAchievements(String token);
    String uploadAchievement(List<MultipartFile> files, List<String> shortDescriptions, String token);
    String deleteAchievement(Long achievementId, String token);
    String getFileUrl(Long achievementId, String token);
    List<UserAchievement> getAllAchievements();
}
