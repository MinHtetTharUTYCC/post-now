package com.minhtetthar.post_now.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateDto {
    
    @Email(message = "Email should be valid")
    private String email;
    
    private String firstName;
    private String lastName;
    
    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;
    
    private String profileImage;
}