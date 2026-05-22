package com.cresensolutions.userservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String uploadFile(MultipartFile file, String s3Key);
    void deleteFile(String s3Key);
    String generateFileUrl(String s3Key);
}
