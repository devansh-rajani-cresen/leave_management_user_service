package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.dto.MyProfile;

public interface MyProfileService {
    MyProfile getMyProfileData(Long userId);
}
