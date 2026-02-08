package com.minhtetthar.post_now.controller;

import com.minhtetthar.post_now.dto.user.UserDto;
import com.minhtetthar.post_now.dto.user.UserUpdateDto;
import com.minhtetthar.post_now.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
}