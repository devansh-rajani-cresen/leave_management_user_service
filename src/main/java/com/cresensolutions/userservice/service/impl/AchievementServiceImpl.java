package com.cresensolutions.userservice.service.impl;

import com.cresensolutions.userservice.entity.UserAchievement;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.UserAchievementRepository;
import com.cresensolutions.userservice.service.AchievementService;
import com.cresensolutions.userservice.service.S3Service;
import com.cresensolutions.userservice.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementServiceImpl implements AchievementService {

    private final JwtUtil jwtUtil;
    private final UserAchievementRepository userAchievementRepository;
    private final S3Service s3Service;

    @Override
    public List<UserAchievement> getMyAchievements(String token) {
        Long userId = jwtUtil.extractUserId(token);
        return userAchievementRepository.findByUserIdAndIsDeletedFalse(userId);
    }

    @Override
    public String uploadAchievement(List<MultipartFile> files,
                                    List<String> shortDescriptions,
                                    String token) {
        if (files.size() != shortDescriptions.size()) {
            throw new CustomException("Files and short descriptions count must match", 400);
        }

        Long userId = jwtUtil.extractUserId(token);
        String username = jwtUtil.extractUsername(token);

        for (int i = 0; i < files.size(); i++) {

            MultipartFile file = files.get(i);
            String shortDescription = shortDescriptions.get(i);

            if (file.isEmpty()) {
                throw new CustomException("File cannot be empty", 400);
            }

            log.info("Uploading File: {}", file.getOriginalFilename());
            log.info("Short Description: {}", shortDescription);

            String storedFileName =
                    UUID.randomUUID() + "_" + file.getOriginalFilename();

            String s3Key =
                    "achievements/" + userId + "_" + username + "/" + storedFileName;

            s3Service.uploadFile(file, s3Key);

            UserAchievement achievement = UserAchievement.builder()
                    .userId(userId)
                    .shortDescription(shortDescription)
                    .originalFileName(file.getOriginalFilename())
                    .storedFileName(storedFileName)
                    .s3Key(s3Key)
                    .fileType(file.getContentType())
                    .fileSize(file.getSize())
                    .uploadedAt(OffsetDateTime.now())
                    .uploadedBy(username)
                    .isDeleted(false)
                    .build();

            userAchievementRepository.save(achievement);
        }
        return "Achievements uploaded!";
    }

    @Override
    public String deleteAchievement(Long achievementId, String token) {
        Long userId = jwtUtil.extractUserId(token);
        UserAchievement achievement = userAchievementRepository
                        .findByIdAndIsDeletedFalse(achievementId)
                        .orElseThrow(() ->
                                new CustomException("Achievement not found", 404));

        if (!achievement.getUserId().equals(userId)) {
            throw new CustomException("Unauthorized", 403);
        }

        s3Service.deleteFile(achievement.getS3Key());
        achievement.setIsDeleted(true);
        userAchievementRepository.save(achievement);
        return "Achievement deleted!";
    }

    @Override
    public String getFileUrl(Long achievementId, String token) {
        Long userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);
        UserAchievement achievement = userAchievementRepository
                        .findByIdAndIsDeletedFalse(achievementId)
                        .orElseThrow(() ->
                                new CustomException("Achievement not found", 404));

        if (!achievement.getUserId().equals(userId) && !"ADMIN".equals(role)) {
            throw new CustomException("Unauthorized", 403);
        }
        return s3Service.generateFileUrl(achievement.getS3Key());
    }

    @Override
    public List<UserAchievement> getAllAchievements() {
        return userAchievementRepository.findByIsDeletedFalse();
    }
}
