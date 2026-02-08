package com.minhtetthar.post_now.dto.post;

import com.minhtetthar.post_now.dto.user.UserSummaryDto;
import com.minhtetthar.post_now.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostDto {
    private Long id;
    private String title;
    private String content;
    private Post.PostType type;
    private String imageUrl;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserSummaryDto author;
    private Long likesCount;
    private Long commentsCount;
    private Boolean likedByCurrentUser;
}