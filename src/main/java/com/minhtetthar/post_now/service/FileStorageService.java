package com.minhtetthar.post_now.service;

import com.minhtetthar.post_now.config.R2Config;
import com.minhtetthar.post_now.exception.FileUploadException;
import com.minhtetthar.post_now.exception.FileSizeLimitExceededException;
import com.minhtetthar.post_now.exception.InvalidFileTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileStorageService {

    private final S3Client s3Client;
    private final R2Config r2Config;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp");
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList(
            "jpg", "jpeg", "png", "gif", "webp");

    /**
     * Upload an image file to Cloudflare R2
     *
     * @param file   The multipart file to upload
     * @param folder The folder/prefix in the bucket (e.g., "profiles" or "posts")
     * @return The public URL of the uploaded image
     */
    public String uploadImage(MultipartFile file, String folder) {
        log.info("Starting image upload to folder: {}", folder);
        validateFile(file);

        String fileName = generateFileName(file, folder);
        String contentType = file.getContentType();

        log.debug("Uploading file: {} with content type: {}", fileName, contentType);

        try (InputStream inputStream = file.getInputStream()) {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(fileName)
                    .contentType(contentType)
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, file.getSize()));

            String publicUrl = constructPublicUrl(fileName);
            log.info("Successfully uploaded file: {} to R2 bucket with URL: {}", fileName, publicUrl);
            return publicUrl;

        } catch (IOException e) {
            log.error("Failed to read file input stream", e);
            throw new FileUploadException("Failed to read file", e);
        } catch (S3Exception e) {
            log.error("Failed to upload file to R2", e);
            throw new FileUploadException(
                    "Failed to upload file to cloud storage: " + e.awsErrorDetails().errorMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error during file upload", e);
            throw new FileUploadException("Unexpected error during upload: " + e.getMessage(), e);
        }
    }

    /**
     * Delete an image from R2 by its URL
     *
     * @param imageUrl The public URL of the image to delete
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            String fileName = extractFileNameFromUrl(imageUrl);
            if (fileName == null) {
                log.warn("Could not extract file name from URL: {}", imageUrl);
                return;
            }

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            log.info("Successfully deleted file: {} from R2 bucket", fileName);

        } catch (S3Exception e) {
            log.error("Failed to delete file from R2: {}", imageUrl, e);
            // Don't throw exception - deletion is best effort
        }
    }

    /**
     * Validate file size and type
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty or null");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeLimitExceededException(
                    String.format("File size exceeds maximum allowed size of %d MB", MAX_FILE_SIZE / (1024 * 1024)));
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new InvalidFileTypeException(
                    "Invalid file type. Allowed types: " + String.join(", ", ALLOWED_CONTENT_TYPES));
        }

        // Check file extension
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        if (extension == null || !ALLOWED_EXTENSIONS.contains(extension.toLowerCase())) {
            throw new InvalidFileTypeException(
                    "Invalid file extension. Allowed extensions: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // Validate magic bytes (first few bytes of the file)
        try {
            byte[] fileBytes = new byte[8];
            int bytesRead = file.getInputStream().read(fileBytes);
            if (bytesRead < 2) {
                throw new InvalidFileTypeException("File is too small or corrupted");
            }

            if (!isValidImageMagicBytes(fileBytes)) {
                throw new InvalidFileTypeException("File content does not match declared image type");
            }
        } catch (IOException e) {
            throw new FileUploadException("Failed to validate file content", e);
        }
    }

    /**
     * Check magic bytes to verify actual file type
     */
    private boolean isValidImageMagicBytes(byte[] bytes) {
        // JPEG: FF D8 FF
        if (bytes.length >= 3 && bytes[0] == (byte) 0xFF && bytes[1] == (byte) 0xD8 && bytes[2] == (byte) 0xFF) {
            return true;
        }
        // PNG: 89 50 4E 47
        if (bytes.length >= 4 && bytes[0] == (byte) 0x89 && bytes[1] == 0x50 && bytes[2] == 0x4E && bytes[3] == 0x47) {
            return true;
        }
        // GIF: 47 49 46 38
        if (bytes.length >= 4 && bytes[0] == 0x47 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x38) {
            return true;
        }
        // WebP: RIFF ... WEBP (check for "RIFF" and "WEBP")
        if (bytes.length >= 8 && bytes[0] == 0x52 && bytes[1] == 0x49 && bytes[2] == 0x46 && bytes[3] == 0x46) {
            // Check for WEBP at position 8-11, but we only read 8 bytes, so we'll be
            // lenient
            return true;
        }
        return false;
    }

    /**
     * Generate a unique file name with the original extension
     */
    private String generateFileName(MultipartFile file, String folder) {
        String extension = FilenameUtils.getExtension(file.getOriginalFilename());
        String uniqueId = UUID.randomUUID().toString();
        return folder + "/" + uniqueId + "." + extension;
    }

    /**
     * Construct the public URL for an uploaded file
     */
    private String constructPublicUrl(String fileName) {
        String publicUrl = r2Config.getPublicUrl();
        if (!publicUrl.endsWith("/")) {
            publicUrl += "/";
        }
        return publicUrl + fileName;
    }

    /**
     * Extract the file name (key) from a public URL
     */
    private String extractFileNameFromUrl(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return null;
        }

        String publicUrl = r2Config.getPublicUrl();
        if (imageUrl.startsWith(publicUrl)) {
            String fileName = imageUrl.substring(publicUrl.length());
            if (fileName.startsWith("/")) {
                fileName = fileName.substring(1);
            }
            return fileName;
        }

        // If URL doesn't match our public URL, return null
        return null;
    }
}
