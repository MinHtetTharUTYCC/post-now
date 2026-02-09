package com.minhtetthar.post_now.service;

import com.minhtetthar.post_now.dto.follow.FollowDto;
import com.minhtetthar.post_now.dto.user.UserSummaryDto;
import com.minhtetthar.post_now.entity.Follow;
import com.minhtetthar.post_now.entity.User;
import com.minhtetthar.post_now.mapper.UserMapper;
import com.minhtetthar.post_now.repository.FollowRepository;
import com.minhtetthar.post_now.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional
    public FollowDto followUser(String followerUsername, String followingUsername) {
        if (followerUsername.equals(followingUsername)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Follower not found: " + followerUsername));

        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + followingUsername));

        if (followRepository.existsByFollowerAndFollowing(follower, following)) {
            throw new IllegalArgumentException("Already following this user");
        }

        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        follow = followRepository.save(follow);

        log.info("User {} followed user {}", followerUsername, followingUsername);

        return mapToDto(follow);
    }

    @Transactional
    public void unfollowUser(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername)
                .orElseThrow(() -> new UsernameNotFoundException("Follower not found: " + followerUsername));

        User following = userRepository.findByUsername(followingUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + followingUsername));

        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new IllegalArgumentException("Not following this user"));

        followRepository.delete(follow);
        log.info("User {} unfollowed user {}", followerUsername, followingUsername);
    }

    public boolean isFollowing(String followerUsername, String followingUsername) {
        User follower = userRepository.findByUsername(followerUsername).orElse(null);
        User following = userRepository.findByUsername(followingUsername).orElse(null);

        if (follower == null || following == null) {
            return false;
        }

        return followRepository.existsByFollowerAndFollowing(follower, following);
    }

    public Page<UserSummaryDto> getFollowers(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return followRepository.findByFollowing(user, pageable)
                .map(follow -> userMapper.toSummaryDto(follow.getFollower()));
    }

    public Page<UserSummaryDto> getFollowing(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return followRepository.findByFollower(user, pageable)
                .map(follow -> userMapper.toSummaryDto(follow.getFollowing()));
    }

    public long getFollowerCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return followRepository.countByFollowing(user);
    }

    public long getFollowingCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return followRepository.countByFollower(user);
    }

    public List<User> getFollowersList(User user) {
        return followRepository.findFollowerUsers(user);
    }

    public User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    private FollowDto mapToDto(Follow follow) {
        FollowDto dto = new FollowDto();
        dto.setId(follow.getId());
        dto.setFollower(userMapper.toSummaryDto(follow.getFollower()));
        dto.setFollowing(userMapper.toSummaryDto(follow.getFollowing()));
        dto.setCreatedAt(follow.getCreatedAt());
        return dto;
    }
}
