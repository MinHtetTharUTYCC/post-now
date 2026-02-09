package com.minhtetthar.post_now.controller;

import com.minhtetthar.post_now.dto.follow.FollowDto;
import com.minhtetthar.post_now.dto.user.UserSummaryDto;
import com.minhtetthar.post_now.entity.User;
import com.minhtetthar.post_now.service.FollowService;
import com.minhtetthar.post_now.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class FollowController {

    private final FollowService followService;
    private final NotificationService notificationService;

    @PostMapping("/{username}/follow")
    public ResponseEntity<?> followUser(
            @PathVariable String username,
            Authentication auth) {
        try {
            String followerUsername = auth.getName();
            log.info("Follow request: {} wants to follow {}", followerUsername, username);

            log.debug("Step 1: Calling followService.followUser()");
            FollowDto follow = followService.followUser(followerUsername, username);
            log.debug("Step 1 SUCCESS: Follow relationship created: {}", follow.getId());

            log.debug("Step 2: Loading follower user: {}", followerUsername);
            User follower = followService.getUser(followerUsername);
            log.debug("Step 2 SUCCESS: Follower loaded - id: {}", follower.getId());

            log.debug("Step 3: Loading following user: {}", username);
            User following = followService.getUser(username);
            log.debug("Step 3 SUCCESS: Following user loaded - id: {}", following.getId());

            log.debug("Step 4: Creating follow notification");
            notificationService.createNewFollowNotification(follower, following);
            log.info("Step 4 SUCCESS: Follow notification created asynchronously");

            return ResponseEntity.status(HttpStatus.CREATED).body(follow);
        } catch (IllegalArgumentException e) {
            log.warn("IllegalArgumentException in follow: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            log.error("Error in followUser endpoint", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to follow user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @DeleteMapping("/{username}/follow")
    public ResponseEntity<?> unfollowUser(
            @PathVariable String username,
            Authentication auth) {
        try {
            String followerUsername = auth.getName();
            log.info("Unfollow request: {} wants to unfollow {}", followerUsername, username);
            followService.unfollowUser(followerUsername, username);
            log.info("Unfollow successful");
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            log.warn("IllegalArgumentException in unfollow: {}", e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            log.error("Error in unfollowUser endpoint", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to unfollow user");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    @GetMapping("/{username}/followers")
    public ResponseEntity<Page<UserSummaryDto>> getFollowers(
            @PathVariable String username,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<UserSummaryDto> followers = followService.getFollowers(username, pageable);
            return ResponseEntity.ok(followers);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{username}/following")
    public ResponseEntity<Page<UserSummaryDto>> getFollowing(
            @PathVariable String username,
            @PageableDefault(size = 20) Pageable pageable) {
        try {
            Page<UserSummaryDto> following = followService.getFollowing(username, pageable);
            return ResponseEntity.ok(following);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{username}/follow-stats")
    public ResponseEntity<Map<String, Object>> getFollowStats(
            @PathVariable String username,
            Authentication auth) {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("followers", followService.getFollowerCount(username));
            stats.put("following", followService.getFollowingCount(username));

            if (auth != null) {
                stats.put("isFollowing", followService.isFollowing(auth.getName(), username));
            }

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
