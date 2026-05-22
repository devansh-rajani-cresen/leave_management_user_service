package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.entity.UserAchievement;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.UserAchievementRepository;
import com.cresensolutions.userservice.service.impl.AchievementServiceImpl;
import com.cresensolutions.userservice.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AchievementServiceImplTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserAchievementRepository userAchievementRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private AchievementServiceImpl achievementService;

    private final String token = "Bearer token";

    private UserAchievement achievement;

    @BeforeEach
    void setUp() {

        achievement = UserAchievement.builder()
                .id(1L)
                .userId(100L)
                .shortDescription("Certificate")
                .s3Key("test-key")
                .isDeleted(false)
                .build();
    }

    @Test
    void testGetMyAchievements() {

        when(jwtUtil.extractUserId(token)).thenReturn(100L);

        when(userAchievementRepository
                .findByUserIdAndIsDeletedFalse(100L))
                .thenReturn(List.of(achievement));

        List<UserAchievement> result =
                achievementService.getMyAchievements(token);

        assertEquals(1, result.size());

        verify(userAchievementRepository)
                .findByUserIdAndIsDeletedFalse(100L);
    }

    @Test
    void testUploadAchievementSuccess() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.pdf",
                        "application/pdf",
                        "data".getBytes()
                );

        when(jwtUtil.extractUserId(token)).thenReturn(100L);

        when(jwtUtil.extractUsername(token)).thenReturn("devansh");

        String result = achievementService.uploadAchievement(
                List.of(file),
                List.of("Certificate"),
                token
        );

        assertEquals("Achievements uploaded!", result);

        verify(s3Service, times(1))
                .uploadFile(any(MultipartFile.class), anyString());

        verify(userAchievementRepository, times(1))
                .save(any(UserAchievement.class));
    }

    @Test
    void testUploadAchievementFileAndDescriptionCountMismatch() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.pdf",
                        "application/pdf",
                        "data".getBytes()
                );

        CustomException exception =
                assertThrows(CustomException.class, () ->
                        achievementService.uploadAchievement(
                                List.of(file),
                                List.of(),
                                token
                        ));

        assertEquals(
                "Files and short descriptions count must match",
                exception.getMessage()
        );
    }

    @Test
    void testUploadAchievementEmptyFile() {

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.pdf",
                        "application/pdf",
                        new byte[]{}
                );

        when(jwtUtil.extractUserId(token)).thenReturn(100L);

        when(jwtUtil.extractUsername(token)).thenReturn("devansh");

        CustomException exception =
                assertThrows(CustomException.class, () ->
                        achievementService.uploadAchievement(
                                List.of(file),
                                List.of("Certificate"),
                                token
                        ));

        assertEquals("File cannot be empty", exception.getMessage());
    }

    @Test
    void testDeleteAchievementSuccess() {

        when(jwtUtil.extractUserId(token)).thenReturn(100L);

        when(userAchievementRepository
                .findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(achievement));

        String result =
                achievementService.deleteAchievement(1L, token);

        assertEquals("Achievement deleted!", result);

        verify(s3Service).deleteFile("test-key");

        verify(userAchievementRepository).save(achievement);

        assertTrue(achievement.getIsDeleted());
    }

    @Test
    void testDeleteAchievementNotFound() {

        when(jwtUtil.extractUserId(token)).thenReturn(100L);

        when(userAchievementRepository
                .findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.empty());

        CustomException exception =
                assertThrows(CustomException.class, () ->
                        achievementService.deleteAchievement(1L, token));

        assertEquals("Achievement not found", exception.getMessage());
    }

    @Test
    void testDeleteAchievementUnauthorized() {

        achievement.setUserId(200L);

        when(jwtUtil.extractUserId(token)).thenReturn(100L);

        when(userAchievementRepository
                .findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(achievement));

        CustomException exception =
                assertThrows(CustomException.class, () ->
                        achievementService.deleteAchievement(1L, token));

        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    void testGetFileUrlSuccess() {

        when(jwtUtil.extractUserId(token)).thenReturn(100L);

        when(jwtUtil.extractRole(token)).thenReturn("USER");

        when(userAchievementRepository
                .findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(achievement));

        when(s3Service.generateFileUrl("test-key"))
                .thenReturn("signed-url");

        String result =
                achievementService.getFileUrl(1L, token);

        assertEquals("signed-url", result);
    }

    @Test
    void testGetFileUrlAdminSuccess() {

        achievement.setUserId(200L);

        when(jwtUtil.extractUserId(token)).thenReturn(100L);

        when(jwtUtil.extractRole(token)).thenReturn("ADMIN");

        when(userAchievementRepository
                .findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(achievement));

        when(s3Service.generateFileUrl("test-key"))
                .thenReturn("signed-url");

        String result =
                achievementService.getFileUrl(1L, token);

        assertEquals("signed-url", result);
    }

    @Test
    void testGetFileUrlUnauthorized() {

        achievement.setUserId(200L);

        when(jwtUtil.extractUserId(token)).thenReturn(100L);

        when(jwtUtil.extractRole(token)).thenReturn("USER");

        when(userAchievementRepository
                .findByIdAndIsDeletedFalse(1L))
                .thenReturn(Optional.of(achievement));

        CustomException exception =
                assertThrows(CustomException.class, () ->
                        achievementService.getFileUrl(1L, token));

        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    void testGetAllAchievements() {

        when(userAchievementRepository.findByIsDeletedFalse())
                .thenReturn(List.of(achievement));

        List<UserAchievement> result =
                achievementService.getAllAchievements();

        assertEquals(1, result.size());

        verify(userAchievementRepository)
                .findByIsDeletedFalse();
    }
}