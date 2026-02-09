package com.minhtetthar.post_now.controller;

import com.minhtetthar.post_now.dto.comment.CommentCreateDto;
import com.minhtetthar.post_now.dto.comment.CommentDto;
import com.minhtetthar.post_now.service.CommentService;
import com.minhtetthar.post_now.service.NotificationService;
import com.minhtetthar.post_now.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final NotificationService notificationService;
    private final UserService userService;

    @GetMapping("/post/{postId}")
    public ResponseEntity<Page<CommentDto>> getCommentsByPostId(
            @PathVariable Long postId,
            @PageableDefault(size = 50) Pageable pageable) {
        try {
            Page<CommentDto> comments = commentService.getCommentsByPostId(postId, pageable);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{username}")
    public ResponseEntity<Page<CommentDto>> getCommentsByUsername(
            @PathVariable String username,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<CommentDto> comments = commentService.getCommentsByUsername(username, pageable);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/post/{postId}")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CommentCreateDto createDto,
            Authentication auth) {
        try {
            CommentDto comment = commentService.createComment(postId, createDto, auth.getName());

            // Trigger notification for post author
            var actor = userService.loadUserByUsername(auth.getName());
            var commentEntity = commentService.getCommentEntity(comment.getId());
            notificationService.createNewCommentNotification(actor, commentEntity);

            return ResponseEntity.status(HttpStatus.CREATED).body(comment);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication auth) {
        try {
            commentService.deleteComment(commentId, auth.getName());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}