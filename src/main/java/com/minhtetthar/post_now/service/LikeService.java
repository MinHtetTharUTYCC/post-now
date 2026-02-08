package com.minhtetthar.post_now.service;

import com.minhtetthar.post_now.entity.Like;
import com.minhtetthar.post_now.entity.Post;
import com.minhtetthar.post_now.entity.User;
import com.minhtetthar.post_now.repository.LikeRepository;
import com.minhtetthar.post_now.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserService userService;

    public Long getLikesCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return likeRepository.countByPost(post);
    }

    public boolean isPostLikedByUser(Long postId, String username) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        User user = userService.loadUserByUsername(username);
        return likeRepository.existsByPostAndUser(post, user);
    }

    @Transactional
    public void likePost(Long postId, String username) {
        Post post = postRepository.findActivePostById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        User user = userService.loadUserByUsername(username);

        // Check if already liked
        if (likeRepository.existsByPostAndUser(post, user)) {
            throw new RuntimeException("You have already liked this post");
        }

        Like like = Like.builder()
                .post(post)
                .user(user)
                .build();

        likeRepository.save(like);
    }

    @Transactional
    public void unlikePost(Long postId, String username) {
        Post post = postRepository.findActivePostById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        User user = userService.loadUserByUsername(username);

        Optional<Like> like = likeRepository.findByPostAndUser(post, user);

        if (like.isEmpty()) {
            throw new RuntimeException("You have not liked this post");
        }

        likeRepository.delete(like.get());
    }

    @Transactional
    public void toggleLike(Long postId, String username) {
        Post post = postRepository.findActivePostById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        User user = userService.loadUserByUsername(username);

        Optional<Like> existingLike = likeRepository.findByPostAndUser(post, user);

        if (existingLike.isPresent()) {
            likeRepository.delete(existingLike.get());
        } else {
            Like like = Like.builder()
                    .post(post)
                    .user(user)
                    .build();
            likeRepository.save(like);
        }
    }
}