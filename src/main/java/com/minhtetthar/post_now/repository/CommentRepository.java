package com.minhtetthar.post_now.repository;

import com.minhtetthar.post_now.entity.Comment;
import com.minhtetthar.post_now.entity.Post;
import com.minhtetthar.post_now.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    Page<Comment> findByPostAndActiveTrueOrderByCreatedAtAsc(Post post, Pageable pageable);
    
    Page<Comment> findByAuthorAndActiveTrueOrderByCreatedAtDesc(User author, Pageable pageable);
    
    @Query("SELECT COUNT(c) FROM Comment c WHERE c.post = :post AND c.active = true")
    Long countByPostAndActiveTrue(@Param("post") Post post);
    
    @Query("SELECT c FROM Comment c WHERE c.id = :id AND c.active = true")
    Comment findActiveCommentById(@Param("id") Long id);
}