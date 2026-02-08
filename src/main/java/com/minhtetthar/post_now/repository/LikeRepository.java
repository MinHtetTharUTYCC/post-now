package com.minhtetthar.post_now.repository;

import com.minhtetthar.post_now.entity.Like;
import com.minhtetthar.post_now.entity.Post;
import com.minhtetthar.post_now.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    Optional<Like> findByPostAndUser(Post post, User user);

    @Query("SELECT COUNT(l) FROM Like l WHERE l.post = :post")
    Long countByPost(@Param("post") Post post);

    @Query("SELECT CASE WHEN COUNT(l) > 0 THEN true ELSE false END FROM Like l WHERE l.post = :post AND l.user = :user")
    boolean existsByPostAndUser(@Param("post") Post post, @Param("user") User user);

    void deleteByPostAndUser(Post post, User user);
}