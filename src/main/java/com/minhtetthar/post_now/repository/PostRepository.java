package com.minhtetthar.post_now.repository;

import com.minhtetthar.post_now.entity.Post;
import com.minhtetthar.post_now.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Page<Post> findByActiveTrue(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.active = true AND p.type = 'PUBLIC' ORDER BY p.createdAt DESC")
    Page<Post> findPublicPosts(Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.active = true AND " +
            "(p.type = 'PUBLIC' OR p.author = :user) ORDER BY p.createdAt DESC")
    Page<Post> findPostsForUser(@Param("user") User user, Pageable pageable);

    Page<Post> findByAuthorAndActiveTrue(User author, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.active = true AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :search, '%'))) ORDER BY p.createdAt DESC")
    Page<Post> findBySearchTerm(@Param("search") String search, Pageable pageable);

    @Query("SELECT p FROM Post p WHERE p.id = :id AND p.active = true")
    Optional<Post> findActivePostById(@Param("id") Long id);
}