package com.cresensolutions.userservice.service;

import com.cresensolutions.userservice.exception.CustomException;
import com.cresensolutions.userservice.service.impl.S3ServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class S3ServiceImplTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private S3ServiceImpl s3Service;

    @BeforeEach
    void setUp() {

        ReflectionTestUtils.setField(
                s3Service,
                "bucketName",
                "test-bucket"
        );

        ReflectionTestUtils.setField(
                s3Service,
                "region",
                "ap-south-1"
        );

        ReflectionTestUtils.setField(
                s3Service,
                "accessKey",
                "access-key"
        );

        ReflectionTestUtils.setField(
                s3Service,
                "secretKey",
                "secret-key"
        );
    }

    @Test
    void testUploadFileSuccess() throws Exception {

        when(multipartFile.getContentType())
                .thenReturn("application/pdf");

        when(multipartFile.getBytes())
                .thenReturn("data".getBytes());

        String result =
                s3Service.uploadFile(multipartFile, "test-key");

        assertEquals("test-key", result);

        verify(s3Client, times(1))
                .putObject((PutObjectRequest) any(), any(RequestBody.class));
    }

    @Test
    void testUploadFileFailure() throws Exception {

        when(multipartFile.getContentType())
                .thenReturn("application/pdf");

        when(multipartFile.getBytes())
                .thenThrow(new RuntimeException());

        CustomException exception =
                assertThrows(CustomException.class, () ->
                        s3Service.uploadFile(
                                multipartFile,
                                "test-key"
                        ));

        assertEquals(
                "Failed to upload file to S3",
                exception.getMessage()
        );
    }

    @Test
    void testDeleteFile() {

        s3Service.deleteFile("test-key");

        verify(s3Client, times(1))
                .deleteObject((DeleteObjectRequest) any());
    }

    @Test
    void testGenerateFileUrl() {

        String result =
                s3Service.generateFileUrl("test-key");

        assertNotNull(result);

        assertTrue(result.contains("test-key"));
    }
}