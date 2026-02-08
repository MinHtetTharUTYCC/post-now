package com.minhtetthar.post_now.dto.user;

import com.minhtetthar.post_now.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String bio;
    private String profileImage;
    private User.Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}