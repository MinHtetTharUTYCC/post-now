package com.minhtetthar.post_now.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    @GetMapping
    public ResponseEntity<?> getAllPosts(Authentication auth) {
        // Return different content based on authentication status
        if (auth != null) {
            // Authenticated user - return all posts (public + private)
            return ResponseEntity.ok(Map.of(
                    "posts", List.of(
                            Map.of("id", 1, "title", "Welcome to PostNow", "type", "public"),
                            Map.of("id", 2, "title", "Getting Started Guide", "type", "public"),
                            Map.of("id", 3, "title", "Private Post 1", "type", "private"),
                            Map.of("id", 4, "title", "Admin Only Content", "type", "private")),
                    "user", auth.getName(),
                    "message", "All posts (authenticated)"));
        } else {
            // Guest user - return only public posts
            return ResponseEntity.ok(Map.of(
                    "posts", List.of(
                            Map.of("id", 1, "title", "Welcome to PostNow", "type", "public"),
                            Map.of("id", 2, "title", "Getting Started Guide", "type", "public")),
                    "message", "Public posts only (guest)"));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Long id, Authentication auth) {
        // Mock post data - in real app this would come from database
        Map<String, Object> post = Map.of(
                "id", id,
                "title", "Sample Post " + id,
                "content", "This is the content of post " + id,
                "author", "admin",
                "timestamp", LocalDateTime.now().minusHours(2),
                "type", "public");

        if (auth != null) {
            // Authenticated user - return post with additional info
            return ResponseEntity.ok(Map.of(
                    "post", post,
                    "user", auth.getName(),
                    "message", "Post details (authenticated)"));
        } else {
            // Guest user - return basic post info
            return ResponseEntity.ok(Map.of(
                    "post", post,
                    "message", "Post details (guest)"));
        }
    }

    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Map<String, String> postData, Authentication auth) {
        return ResponseEntity.ok(Map.of(
                "message", "Post created successfully",
                "title", postData.get("title"),
                "author", auth.getName(),
                "timestamp", LocalDateTime.now()));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<?> likePost(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(Map.of(
                "message", "Post liked",
                "postId", id,
                "likedBy", auth.getName(),
                "timestamp", LocalDateTime.now()));
    }

    @PostMapping("/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Map<String, String> commentData,
            Authentication auth) {
        return ResponseEntity.ok(Map.of(
                "message", "Comment added",
                "postId", id,
                "comment", commentData.get("content"),
                "author", auth.getName(),
                "timestamp", LocalDateTime.now()));
    }

}