package com.minhtetthar.post_now.dto.user;

import lombok.Data;

@Data
public class UserSummaryDto {
    private Long id;
    private String username;
    private String firstName;
    private String lastName;
    private String profileImage;
}