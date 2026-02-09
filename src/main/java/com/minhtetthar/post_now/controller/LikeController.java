package com.minhtetthar.post_now.controller;

import com.minhtetthar.post_now.service.LikeService;
import com.minhtetthar.post_now.service.NotificationService;
import com.minhtetthar.post_now.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/post/{postId}/count")
    public ResponseEntity<Map<String, Long>> getLikesCount(@PathVariable Long postId) {
        try {
            Long count = likeService.getLikesCount(postId);
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/post/{postId}/status")
    public ResponseEntity<Map<String, Boolean>> isPostLikedByUser(
            @PathVariable Long postId,
            Authentication auth) {
        try {
            boolean liked = likeService.isPostLikedByUser(postId, auth.getName());
            return ResponseEntity.ok(Map.of("liked", liked));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<Map<String, String>> likePost(
            @PathVariable Long postId,
            Authentication auth) {
        try {
            likeService.likePost(postId, auth.getName());

            // Trigger notification for post author
            var actor = userService.loadUserByUsername(auth.getName());
            var post = likeService.getPost(postId);
            notificationService.createNewLikeNotification(actor, post);

            return ResponseEntity.ok(Map.of("message", "Post liked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Map<String, String>> unlikePost(
            @PathVariable Long postId,
            Authentication auth) {
        try {
            likeService.unlikePost(postId, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Post unliked successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/post/{postId}/toggle")
    public ResponseEntity<Map<String, String>> toggleLike(
            @PathVariable Long postId,
            Authentication auth) {
        try {
            likeService.toggleLike(postId, auth.getName());
            return ResponseEntity.ok(Map.of("message", "Like toggled successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

/*
 * Just testing wow haha
 */