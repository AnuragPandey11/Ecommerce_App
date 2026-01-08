package com.ecom.util;



import com.ecom.exception.FileUploadException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class FileUploadUtil {

    @Value("${app.upload.directory:${user.home}/ecommerce-uploads}")
    private String uploadDir;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    /**
     * Upload a single file
     */
    public String uploadFile(MultipartFile file, String subDirectory) {
        validateFile(file);

        try {
            // Create directory if not exists
            Path uploadPath = Paths.get(uploadDir, subDirectory);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String uniqueFilename = UUID.randomUUID().toString() + "." + extension;

            // Save file
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            log.info("File uploaded successfully: {}", uniqueFilename);

            // Return relative URL
            return "/" + subDirectory + "/" + uniqueFilename;

        } catch (IOException e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new FileUploadException("Could not upload file: " + file.getOriginalFilename());
        }
    }

    /**
     * Delete a file
     */
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || fileUrl.isEmpty()) {
                return;
            }

            // Remove leading slash
            String relativePath = fileUrl.startsWith("/") ? fileUrl.substring(1) : fileUrl;
            Path filePath = Paths.get(uploadDir, relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("File deleted successfully: {}", fileUrl);
            }
        } catch (IOException e) {
            log.error("Failed to delete file: {}", e.getMessage());
            throw new FileUploadException("Could not delete file: " + fileUrl);
        }
    }

    /**
     * Get file path for serving
     */
    public Path getFilePath(String filename) {
        return Paths.get(uploadDir).resolve(filename).normalize();
    }

    /**
     * Validate file before upload
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException("File size exceeds maximum limit of 5MB");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new FileUploadException("File type not allowed. Allowed types: " + ALLOWED_EXTENSIONS);
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileUploadException("Invalid file type. Only images are allowed");
        }
    }

    /**
     * Get file extension
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new FileUploadException("Invalid filename");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }

    /**
     * Create directory structure
     */
    public void createDirectories() {
        try {
            Files.createDirectories(Paths.get(uploadDir, "products"));
            Files.createDirectories(Paths.get(uploadDir, "categories"));
            Files.createDirectories(Paths.get(uploadDir, "users"));
            Files.createDirectories(Paths.get(uploadDir, "brands"));
            log.info("Upload directories created successfully");
        } catch (IOException e) {
            log.error("Failed to create upload directories: {}", e.getMessage());
        }
    }
}

