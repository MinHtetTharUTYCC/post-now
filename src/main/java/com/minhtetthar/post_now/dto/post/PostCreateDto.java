package com.minhtetthar.post_now.dto.post;

import com.minhtetthar.post_now.entity.Post;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostCreateDto {
    
    @NotBlank(message = "Title is required")
    @Size(max = 1000, message = "Title cannot exceed 1000 characters")
    private String title;
    
    private String content;
    
    private Post.PostType type = Post.PostType.PUBLIC;
    
    private String imageUrl;
}