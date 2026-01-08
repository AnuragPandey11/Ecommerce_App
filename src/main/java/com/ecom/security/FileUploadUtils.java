package com.ecom.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.UUID;

@Component
@Slf4j
public class FileUploadUtils {

    @Value("${app.upload.directory}")
    private String uploadDir;

    public String storeFile(MultipartFile file) {
        String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = "";

        int dotIndex = originalFileName.lastIndexOf('.');
        if (dotIndex > 0) {
            fileExtension = originalFileName.substring(dotIndex);
        }

        String fileName = Instant.now().toEpochMilli()
                + "-" + UUID.randomUUID() + fileExtension;

        try {
            Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(uploadPath);

            Path targetLocation = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation,
                    StandardCopyOption.REPLACE_EXISTING);

            return "/api/images/" + fileName;
        } catch (IOException ex) {
            log.error("Could not store file {}", originalFileName, ex);
            throw new RuntimeException("Could not store file. Please try again.");
        }
    }
}
