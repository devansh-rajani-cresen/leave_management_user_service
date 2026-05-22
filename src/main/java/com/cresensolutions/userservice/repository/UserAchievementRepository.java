package com.cresensolutions.userservice.repository;

import com.cresensolutions.userservice.entity.UserAchievement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAchievementRepository extends JpaRepository<UserAchievement, Long> {
    List<UserAchievement> findByUserIdAndIsDeletedFalse(Long userId);
    Optional<UserAchievement> findByIdAndIsDeletedFalse(Long id);
    List<UserAchievement> findByIsDeletedFalse();
}