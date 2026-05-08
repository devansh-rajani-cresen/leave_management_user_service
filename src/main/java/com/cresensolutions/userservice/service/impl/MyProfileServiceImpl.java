package com.cresensolutions.userservice.service.impl;

import com.cresensolutions.userservice.dto.MyProfile;
import com.cresensolutions.userservice.entity.User;
import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.repository.UserRepository;
import com.cresensolutions.userservice.service.MyProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyProfileServiceImpl implements MyProfileService {
    private final UserRepository userRepository;

    @Override
    public MyProfile getMyProfileData(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException("User not found!", 404));

        MyProfile dto = new MyProfile();
        dto.setCompanyId(user.getCompanyId());
        dto.setUserName(user.getUserName());
        dto.setFullName(user.getFullName());
        dto.setEmailId(user.getEmailId());
        dto.setRole(user.getRoleName());
        dto.setGender(user.getGender());
        dto.setActive(user.getActive());

        return dto;
    }
}
