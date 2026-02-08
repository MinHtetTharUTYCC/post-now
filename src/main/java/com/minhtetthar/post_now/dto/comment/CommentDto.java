package com.minhtetthar.post_now.dto.comment;

import com.minhtetthar.post_now.dto.user.UserSummaryDto;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long id;
    private String content;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummaryDto author;
    private Long postId;
}