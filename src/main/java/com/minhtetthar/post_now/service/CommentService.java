package com.minhtetthar.post_now.service;

import com.minhtetthar.post_now.dto.comment.CommentCreateDto;
import com.minhtetthar.post_now.dto.comment.CommentDto;
import com.minhtetthar.post_now.entity.Comment;
import com.minhtetthar.post_now.entity.Post;
import com.minhtetthar.post_now.entity.User;
import com.minhtetthar.post_now.mapper.CommentMapper;
import com.minhtetthar.post_now.repository.CommentRepository;
import com.minhtetthar.post_now.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;

    public Page<CommentDto> getCommentsByPostId(Long postId, Pageable pageable) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        return commentRepository.findByPostAndActiveTrueOrderByCreatedAtAsc(post, pageable)
                .map(commentMapper::toDto);
    }

    public Page<CommentDto> getCommentsByUsername(String username, Pageable pageable) {
        User user = userService.loadUserByUsername(username);
        return commentRepository.findByAuthorAndActiveTrueOrderByCreatedAtDesc(user, pageable)
                .map(commentMapper::toDto);
    }

    @Transactional
    public CommentDto createComment(Long postId, CommentCreateDto createDto, String username) {
        Post post = postRepository.findActivePostById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        User author = userService.loadUserByUsername(username);

        Comment comment = commentMapper.toEntity(createDto);
        comment.setPost(post);
        comment.setAuthor(author);
        comment = commentRepository.save(comment);

        return commentMapper.toDto(comment);
    }

    @Transactional
    public void deleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findActiveCommentById(commentId);

        if (comment == null) {
            throw new RuntimeException("Comment not found with id: " + commentId);
        }

        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new RuntimeException("You don't have permission to delete this comment");
        }

        comment.setActive(false);
        commentRepository.save(comment);
    }
}