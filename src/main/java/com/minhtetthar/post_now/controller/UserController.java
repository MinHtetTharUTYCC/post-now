package com.minhtetthar.post_now.controller;

import com.minhtetthar.post_now.dto.user.UserDto;
import com.minhtetthar.post_now.dto.user.UserUpdateDto;
import com.minhtetthar.post_now.exception.FileUploadException;
import com.minhtetthar.post_now.exception.FileSizeLimitExceededException;
import com.minhtetthar.post_now.exception.InvalidFileTypeException;
import com.minhtetthar.post_now.service.FileStorageService;
import com.minhtetthar.post_now.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final FileStorageService fileStorageService;

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(Authentication auth) {
        try {
            UserDto user = userService.getCurrentUser(auth.getName());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserDto> getUserByUsername(@PathVariable String username) {
        try {
            UserDto user = userService.getUserByUsername(username);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserDto> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<UserDto>> searchUsers(
            @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<UserDto> users = userService.searchUsers(query, pageable);
        return ResponseEntity.ok(users);
    }

    @PutMapping("/me")
    public ResponseEntity<UserDto> updateCurrentUser(
            @Valid @RequestBody UserUpdateDto updateDto,
            Authentication auth) {
        try {
            UserDto user = userService.updateUser(auth.getName(), updateDto);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(Authentication auth) {
        try {
            userService.deleteUser(auth.getName());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/me/profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @RequestParam("image") MultipartFile image,
            Authentication auth) {
        try {
            // Get current user to check for existing image
            UserDto currentUser = userService.getCurrentUser(auth.getName());
            String oldImageUrl = currentUser.getProfileImage();

            // Upload new image
            String imageUrl = fileStorageService.uploadImage(image, "profiles");

            // Update user with new image URL
            UserUpdateDto updateDto = new UserUpdateDto();
            updateDto.setProfileImage(imageUrl);
            UserDto updatedUser = userService.updateUser(auth.getName(), updateDto);

            // Delete old image if it exists
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                fileStorageService.deleteImage(oldImageUrl);
            }

            return ResponseEntity.ok(updatedUser);

        } catch (InvalidFileTypeException | FileSizeLimitExceededException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (FileUploadException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "An unexpected error occurred");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<?> deleteProfileImage(Authentication auth) {
        try {
            UserDto currentUser = userService.getCurrentUser(auth.getName());
            String imageUrl = currentUser.getProfileImage();

            if (imageUrl == null || imageUrl.isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "No profile image to delete");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            // Remove image URL from user
            UserUpdateDto updateDto = new UserUpdateDto();
            updateDto.setProfileImage(null);
            UserDto updatedUser = userService.updateUser(auth.getName(), updateDto);

            // Delete image from storage
            fileStorageService.deleteImage(imageUrl);

            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to delete profile image");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}