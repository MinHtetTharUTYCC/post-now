package com.minhtetthar.post_now.service;

import com.minhtetthar.post_now.dto.post.PostCreateDto;
import com.minhtetthar.post_now.dto.post.PostDto;
import com.minhtetthar.post_now.dto.post.PostUpdateDto;
import com.minhtetthar.post_now.entity.Post;
import com.minhtetthar.post_now.entity.User;
import com.minhtetthar.post_now.mapper.PostMapper;
import com.minhtetthar.post_now.repository.CommentRepository;
import com.minhtetthar.post_now.repository.LikeRepository;
import com.minhtetthar.post_now.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final LikeRepository likeRepository;
    private final PostMapper postMapper;
    private final UserService userService;

    @Cacheable(value = "posts", key = "#id")
    public PostDto getPostById(Long id, String currentUsername) {
        Post post = postRepository.findActivePostById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
        return enrichPostDto(post, currentUsername);
    }

    public Page<PostDto> getAllPosts(Pageable pageable, String currentUsername) {
        Page<Post> posts;

        if (currentUsername != null) {
            User user = userService.loadUserByUsername(currentUsername);
            posts = postRepository.findPostsForUser(user, pageable);
        } else {
            posts = postRepository.findPublicPosts(pageable);
        }

        return posts.map(post -> enrichPostDto(post, currentUsername));
    }

    public Page<PostDto> getPostsByAuthor(String username, Pageable pageable, String currentUsername) {
        User author = userService.loadUserByUsername(username);
        return postRepository.findByAuthorAndActiveTrue(author, pageable)
                .map(post -> enrichPostDto(post, currentUsername));
    }

    public Page<PostDto> searchPosts(String searchTerm, Pageable pageable, String currentUsername) {
        return postRepository.findBySearchTerm(searchTerm, pageable)
                .map(post -> enrichPostDto(post, currentUsername));
    }

    @Transactional
    @CacheEvict(value = { "posts", "postStats" }, allEntries = true)
    public PostDto createPost(PostCreateDto createDto, String username) {
        User author = userService.loadUserByUsername(username);

        Post post = postMapper.toEntity(createDto);
        post.setAuthor(author);
        post = postRepository.save(post);

        return enrichPostDto(post, username);
    }

    @Transactional
    @CacheEvict(value = { "posts", "postStats" }, allEntries = true)
    public PostDto updatePost(Long id, PostUpdateDto updateDto, String username) {
        Post post = postRepository.findActivePostById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        if (!post.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("You don't have permission to update this post");
        }

        postMapper.updateEntity(post, updateDto);
        post = postRepository.save(post);

        return enrichPostDto(post, username);
    }

    @Transactional
    @CacheEvict(value = { "posts", "postStats" }, allEntries = true)
    public void deletePost(Long id, String username) {
        Post post = postRepository.findActivePostById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));

        if (!post.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("You don't have permission to delete this post");
        }

        post.setActive(false);
        postRepository.save(post);
    }

    public Post getPostEntity(Long id) {
        return postRepository.findActivePostById(id)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + id));
    }

    @Cacheable(value = "postStats", key = "#post.id + '-' + #currentUsername")
    private PostDto enrichPostDto(Post post, String currentUsername) {
        PostDto dto = postMapper.toDto(post);
        dto.setLikesCount(likeRepository.countByPost(post));
        dto.setCommentsCount(commentRepository.countByPostAndActiveTrue(post));

        if (currentUsername != null) {
            User user = userService.loadUserByUsername(currentUsername);
            dto.setLikedByCurrentUser(likeRepository.existsByPostAndUser(post, user));
        } else {
            dto.setLikedByCurrentUser(false);
        }

        return dto;
    }
}