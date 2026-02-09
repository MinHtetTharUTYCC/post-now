package com.minhtetthar.post_now.dto.notification;

import com.minhtetthar.post_now.dto.user.UserSummaryDto;
import com.minhtetthar.post_now.entity.Notification;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDto {
    private Long id;
    private Notification.NotificationType type;
    private UserSummaryDto actor;
    private Long postId;
    private Long commentId;
    private boolean read;
    private LocalDateTime createdAt;
    private String message; // Formatted message for display
}
