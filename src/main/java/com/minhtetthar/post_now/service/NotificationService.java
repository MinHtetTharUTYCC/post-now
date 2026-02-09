package com.minhtetthar.post_now.service;

import com.minhtetthar.post_now.dto.notification.NotificationDto;
import com.minhtetthar.post_now.entity.*;
import com.minhtetthar.post_now.mapper.UserMapper;
import com.minhtetthar.post_now.repository.FollowRepository;
import com.minhtetthar.post_now.repository.NotificationRepository;
import com.minhtetthar.post_now.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final FollowRepository followRepository;
    private final EmailService emailService;

    @Async
    @Transactional
    public void createNewPostNotification(User actor, Post post) {
        log.info("Creating new post notifications for user: {}", actor.getUsername());

        // Get all followers
        List<User> followers = followRepository.findFollowerUsers(actor);

        for (User follower : followers) {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.NEW_POST);
            notification.setUser(follower);
            notification.setActor(actor);
            notification.setPost(post);
            notification.setRead(false);

            notificationRepository.save(notification);
        }

        // Send email notifications to followers
        emailService.sendNewPostEmails(actor, post, followers);

        log.info("Created {} new post notifications", followers.size());
    }

    @Async
    @Transactional
    public void createNewLikeNotification(User actor, Post post) {
        // Don't notify if user likes their own post
        if (actor.getId().equals(post.getAuthor().getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.NEW_LIKE);
        notification.setUser(post.getAuthor());
        notification.setActor(actor);
        notification.setPost(post);
        notification.setRead(false);

        notificationRepository.save(notification);
        log.info("Created new like notification for post: {}", post.getId());
    }

    @Async
    @Transactional
    public void createNewCommentNotification(User actor, Comment comment) {
        Post post = comment.getPost();

        // Don't notify if user comments on their own post
        if (actor.getId().equals(post.getAuthor().getId())) {
            return;
        }

        Notification notification = new Notification();
        notification.setType(Notification.NotificationType.NEW_COMMENT);
        notification.setUser(post.getAuthor());
        notification.setActor(actor);
        notification.setPost(post);
        notification.setComment(comment);
        notification.setRead(false);

        notificationRepository.save(notification);
        log.info("Created new comment notification for post: {}", post.getId());
    }

    @Async
    @Transactional
    public void createNewFollowNotification(User follower, User following) {
        log.info("Async createNewFollowNotification started: {} -> {}", 
                follower.getUsername(), following.getUsername());
        try {
            Notification notification = new Notification();
            notification.setType(Notification.NotificationType.NEW_FOLLOW);
            notification.setUser(following);
            notification.setActor(follower);
            notification.setRead(false);

            log.debug("Saving follow notification to database");
            notificationRepository.save(notification);
            log.info("Successfully created new follow notification: {} followed {}", 
                    follower.getUsername(), following.getUsername());
        } catch (Exception e) {
            log.error("Error creating new follow notification", e);
            throw e;
        }
    }

    public Page<NotificationDto> getUserNotifications(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return notificationRepository.findByUserOrderByCreatedAtDesc(user, pageable)
                .map(this::mapToDto);
    }

    public Page<NotificationDto> getUnreadNotifications(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return notificationRepository.findByUserAndReadOrderByCreatedAtDesc(user, false, pageable)
                .map(this::mapToDto);
    }

    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return notificationRepository.countByUserAndRead(user, false);
    }

    @Transactional
    public void markAsRead(String username, Long notificationId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        int updated = notificationRepository.markAsRead(notificationId, user);
        if (updated == 0) {
            throw new IllegalArgumentException("Notification not found or does not belong to user");
        }
    }

    @Transactional
    public void markAllAsRead(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        int updated = notificationRepository.markAllAsReadByUser(user);
        log.info("Marked {} notifications as read for user: {}", updated, username);
    }

    private NotificationDto mapToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setType(notification.getType());
        dto.setActor(userMapper.toSummaryDto(notification.getActor()));
        dto.setPostId(notification.getPost() != null ? notification.getPost().getId() : null);
        dto.setCommentId(notification.getComment() != null ? notification.getComment().getId() : null);
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setMessage(formatNotificationMessage(notification));
        return dto;
    }

    private String formatNotificationMessage(Notification notification) {
        String actorName = notification.getActor().getUsername();

        return switch (notification.getType()) {
            case NEW_POST -> actorName + " posted something new";
            case NEW_LIKE -> actorName + " liked your post";
            case NEW_COMMENT -> actorName + " commented on your post";
            case NEW_FOLLOW -> actorName + " started following you";
        };
    }
}
