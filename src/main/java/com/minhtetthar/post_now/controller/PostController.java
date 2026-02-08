package com.minhtetthar.post_now.controller;

import com.minhtetthar.post_now.dto.post.PostCreateDto;
import com.minhtetthar.post_now.dto.post.PostDto;
import com.minhtetthar.post_now.dto.post.PostUpdateDto;
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

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

        private final PostService postService;

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