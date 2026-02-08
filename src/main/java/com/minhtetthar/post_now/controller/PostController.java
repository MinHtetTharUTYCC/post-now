package com.minhtetthar.post_now.controller;

import com.minhtetthar.post_now.dto.post.PostCreateDto;
import com.minhtetthar.post_now.dto.post.PostDto;
import com.minhtetthar.post_now.dto.post.PostUpdateDto;
import com.minhtetthar.post_now.exception.FileUploadException;
import com.minhtetthar.post_now.exception.FileSizeLimitExceededException;
import com.minhtetthar.post_now.exception.InvalidFileTypeException;
import com.minhtetthar.post_now.service.FileStorageService;
import com.minhtetthar.post_now.service.PostService;
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
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

        private final PostService postService;
        private final FileStorageService fileStorageService;

        @GetMapping
        public ResponseEntity<Page<PostDto>> getAllPosts(
                        @PageableDefault(size = 20) Pageable pageable,
                        Authentication auth) {
                String username = auth != null ? auth.getName() : null;
                Page<PostDto> posts = postService.getAllPosts(pageable, username);
                return ResponseEntity.ok(posts);
        }

        @GetMapping("/{id}")
        public ResponseEntity<PostDto> getPostById(
                        @PathVariable Long id,
                        Authentication auth) {
                try {
                        String username = auth != null ? auth.getName() : null;
                        PostDto post = postService.getPostById(id, username);
                        return ResponseEntity.ok(post);
                } catch (Exception e) {
                        return ResponseEntity.notFound().build();
                }
        }

        @GetMapping("/search")
        public ResponseEntity<Page<PostDto>> searchPosts(
                        @RequestParam String query,
                        @PageableDefault(size = 20) Pageable pageable,
                        Authentication auth) {
                String username = auth != null ? auth.getName() : null;
                Page<PostDto> posts = postService.searchPosts(query, pageable, username);
                return ResponseEntity.ok(posts);
        }

        @GetMapping("/user/{username}")
        public ResponseEntity<Page<PostDto>> getPostsByAuthor(
                        @PathVariable String username,
                        @PageableDefault(size = 20) Pageable pageable,
                        Authentication auth) {
                try {
                        String currentUsername = auth != null ? auth.getName() : null;
                        Page<PostDto> posts = postService.getPostsByAuthor(username, pageable, currentUsername);
                        return ResponseEntity.ok(posts);
                } catch (Exception e) {
                        return ResponseEntity.notFound().build();
                }
        }

        @PostMapping
        public ResponseEntity<PostDto> createPost(
                        @Valid @RequestBody PostCreateDto createDto,
                        Authentication auth) {
                try {
                        PostDto post = postService.createPost(createDto, auth.getName());
                        return ResponseEntity.status(HttpStatus.CREATED).body(post);
                } catch (Exception e) {
                        return ResponseEntity.badRequest().build();
                }
        }

        @PostMapping(consumes = "multipart/form-data")
        public ResponseEntity<?> createPostWithImage(
                        @RequestParam("title") String title,
                        @RequestParam("content") String content,
                        @RequestParam(value = "image", required = false) MultipartFile image,
                        Authentication auth) {
                try {
                        PostCreateDto createDto = new PostCreateDto();
                        createDto.setTitle(title);
                        createDto.setContent(content);

                        // Upload image if provided
                        if (image != null && !image.isEmpty()) {
                                String imageUrl = fileStorageService.uploadImage(image, "posts");
                                createDto.setImageUrl(imageUrl);
                        }

                        PostDto post = postService.createPost(createDto, auth.getName());
                        return ResponseEntity.status(HttpStatus.CREATED).body(post);

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
                        error.put("error", "Failed to create post: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
        }

        @PutMapping("/{id}")
        public ResponseEntity<PostDto> updatePost(
                        @PathVariable Long id,
                        @Valid @RequestBody PostUpdateDto updateDto,
                        Authentication auth) {
                try {
                        PostDto post = postService.updatePost(id, updateDto, auth.getName());
                        return ResponseEntity.ok(post);
                } catch (Exception e) {
                        return ResponseEntity.badRequest().build();
                }
        }

        @PostMapping(value = "/{id}/image", consumes = "multipart/form-data")
        public ResponseEntity<?> updatePostImage(
                        @PathVariable Long id,
                        @RequestParam("image") MultipartFile image,
                        Authentication auth) {
                try {
                        // Get current post to check for existing image
                        PostDto currentPost = postService.getPostById(id, auth.getName());
                        String oldImageUrl = currentPost.getImageUrl();

                        // Upload new image
                        String imageUrl = fileStorageService.uploadImage(image, "posts");

                        // Update post with new image URL
                        PostUpdateDto updateDto = new PostUpdateDto();
                        updateDto.setImageUrl(imageUrl);
                        PostDto updatedPost = postService.updatePost(id, updateDto, auth.getName());

                        // Delete old image if it exists
                        if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                                fileStorageService.deleteImage(oldImageUrl);
                        }

                        return ResponseEntity.ok(updatedPost);

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
                        error.put("error", "Failed to update post image: " + e.getMessage());
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
        }

        @DeleteMapping("/{id}/image")
        public ResponseEntity<?> deletePostImage(
                        @PathVariable Long id,
                        Authentication auth) {
                try {
                        PostDto currentPost = postService.getPostById(id, auth.getName());
                        String imageUrl = currentPost.getImageUrl();

                        if (imageUrl == null || imageUrl.isEmpty()) {
                                Map<String, String> error = new HashMap<>();
                                error.put("error", "No image to delete");
                                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                        }

                        // Remove image URL from post
                        PostUpdateDto updateDto = new PostUpdateDto();
                        updateDto.setImageUrl(null);
                        PostDto updatedPost = postService.updatePost(id, updateDto, auth.getName());

                        // Delete image from storage
                        fileStorageService.deleteImage(imageUrl);

                        return ResponseEntity.ok(updatedPost);

                } catch (Exception e) {
                        Map<String, String> error = new HashMap<>();
                        error.put("error", "Failed to delete post image");
                        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
                }
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deletePost(
                        @PathVariable Long id,
                        Authentication auth) {
                try {
                        postService.deletePost(id, auth.getName());
                        return ResponseEntity.noContent().build();
                } catch (Exception e) {
                        return ResponseEntity.badRequest().build();
                }
        }
}