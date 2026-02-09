package com.minhtetthar.post_now.repository;

import com.minhtetthar.post_now.entity.Follow;
import com.minhtetthar.post_now.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    boolean existsByFollowerAndFollowing(User follower, User following);

    long countByFollowing(User following);

    long countByFollower(User follower);

    Page<Follow> findByFollowing(User following, Pageable pageable);

    Page<Follow> findByFollower(User follower, Pageable pageable);

    @Query("SELECT f.following FROM Follow f WHERE f.follower = :user")
    List<User> findFollowingUsers(@Param("user") User user);

    @Query("SELECT f.follower FROM Follow f WHERE f.following = :user")
    List<User> findFollowerUsers(@Param("user") User user);
}
