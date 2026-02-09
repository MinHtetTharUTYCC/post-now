# Notification Integration Summary

## Overview
The notification system has been successfully integrated into the application. Users now receive real-time notifications for social interactions and email notifications when their followed users create new posts.

## Notification Types
1. **NEW_POST** - When a followed user creates a new post (web + email notification)
2. **NEW_LIKE** - When someone likes your post (web notification only)
3. **NEW_COMMENT** - When someone comments on your post (web notification only)
4. **NEW_FOLLOW** - When someone follows you (web notification only)

## Integration Points

### 1. PostController
- **Method**: `createPost()` and `createPostWithImage()`
- **Trigger**: After successful post creation
- **Notification**: Sends notification to all followers
- **Email**: Sends email to all followers via Resend

### 2. LikeController
- **Method**: `likePost()`
- **Trigger**: After successful like operation
- **Notification**: Sends notification to post author
- **Email**: None

### 3. CommentController
- **Method**: `createComment()`
- **Trigger**: After successful comment creation
- **Notification**: Sends notification to post author
- **Email**: None

### 4. FollowController
- **Method**: `followUser()`
- **Trigger**: After successful follow operation
- **Notification**: Sends notification to followed user
- **Email**: None

## API Endpoints

### Follow Endpoints
- `POST /api/follow/{username}` - Follow a user
- `DELETE /api/follow/{username}` - Unfollow a user
- `GET /api/follow/followers` - Get your followers list
- `GET /api/follow/following` - Get who you're following
- `GET /api/follow/status/{username}` - Check if you're following a user
- `GET /api/follow/stats` - Get follower/following counts

### Notification Endpoints
- `GET /api/notifications` - Get all notifications (paginated)
- `GET /api/notifications/unread` - Get unread notifications
- `GET /api/notifications/unread/count` - Get unread notification count
- `PUT /api/notifications/{id}/read` - Mark notification as read
- `PUT /api/notifications/{id}/unread` - Mark notification as unread
- `PUT /api/notifications/read-all` - Mark all notifications as read

## Configuration Required

### 1. Resend API Key
Update your `.env` file with a valid Resend API key:
```properties
RESEND_API_KEY=re_your_actual_api_key_here
```

Get your API key from: https://resend.com/api-keys

### 2. Email Configuration
The system uses the following default sender email:
- **From**: `onboarding@resend.dev` (for testing)

For production, configure a verified domain in Resend and update [EmailService.java](src/main/java/com/minhtetthar/post_now/service/EmailService.java):
```java
.from("noreply@yourdomain.com")
```

## Async Processing
Notifications are processed asynchronously to avoid blocking the main request threads:
- `@EnableAsync` is configured in [PostNowApplication.java](src/main/java/com/minhtetthar/post_now/PostNowApplication.java)
- All notification methods in [NotificationService.java](src/main/java/com/minhtetthar/post_now/service/NotificationService.java) are annotated with `@Async`
- Email sending in [EmailService.java](src/main/java/com/minhtetthar/post_now/service/EmailService.java) is also async

## Testing Steps

### 1. Follow System
```bash
# Follow a user
curl -X POST http://localhost:8080/api/follow/username \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Check your followers
curl http://localhost:8080/api/follow/followers \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 2. Create Post (Triggers Notifications)
```bash
curl -X POST http://localhost:8080/api/posts \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title":"Test Post","content":"This should notify followers"}'
```

### 3. Check Notifications
```bash
# Get unread count
curl http://localhost:8080/api/notifications/unread/count \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Get all notifications
curl http://localhost:8080/api/notifications \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 4. Like a Post (Triggers Notification)
```bash
curl -X POST http://localhost:8080/api/likes/post/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### 5. Comment on a Post (Triggers Notification)
```bash
curl -X POST http://localhost:8080/api/comments/post/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"content":"Great post!"}'
```

## Email Template
The new post email includes:
- Post author information
- Post title and content preview
- Direct link to the post
- Professional HTML styling

## Database Schema

### Follow Table
```sql
CREATE TABLE follow (
    id BIGINT PRIMARY KEY,
    follower_id BIGINT NOT NULL,
    following_id BIGINT NOT NULL,
    created_at TIMESTAMP,
    UNIQUE(follower_id, following_id)
);
```

### Notification Table
```sql
CREATE TABLE notification (
    id BIGINT PRIMARY KEY,
    recipient_id BIGINT NOT NULL,
    actor_id BIGINT NOT NULL,
    type VARCHAR(50) NOT NULL,
    entity_id BIGINT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP
);
```

## Important Notes

### Self-Follow Prevention
Users cannot follow themselves - the system checks and prevents this in [FollowService.java](src/main/java/com/minhtetthar/post_now/service/FollowService.java).

### No Self-Notification
Users don't receive notifications for their own actions - notifications are only created when:
- Someone else likes/comments on your post
- Someone else follows you
- Someone you follow creates a new post

### Async Error Handling
Notification failures won't block the main operations. If notification or email sending fails, the post/like/comment operation still succeeds.

## Troubleshooting

### Notifications Not Appearing
1. Check if you're following the user (for NEW_POST notifications)
2. Verify the actor is not the same as the recipient
3. Check database for notification records
4. Ensure async is enabled in application config

### Emails Not Sending
1. Verify `RESEND_API_KEY` in `.env` file
2. Check Resend dashboard for error logs
3. Verify sender email domain is configured
4. Check application logs for async exceptions

### Performance Issues
If you have many followers, email sending might take time. Consider:
1. Implementing batch email sending
2. Using a message queue (RabbitMQ, Kafka)
3. Rate limiting notifications per user

## Next Steps

1. **Add Resend API Key**: Update `.env` with your actual Resend API key
2. **Test Email Flow**: Create a post and verify followers receive emails
3. **Production Email**: Configure a verified domain in Resend
4. **Frontend Integration**: Build UI for notification bell and follow buttons
5. **Real-time Updates**: Consider WebSocket integration for instant notifications
6. **Notification Preferences**: Allow users to configure which notifications they want

## Related Files
- [NotificationService.java](src/main/java/com/minhtetthar/post_now/service/NotificationService.java) - Core notification logic
- [EmailService.java](src/main/java/com/minhtetthar/post_now/service/EmailService.java) - Email sending with Resend
- [FollowService.java](src/main/java/com/minhtetthar/post_now/service/FollowService.java) - Follow/unfollow logic
- [NotificationController.java](src/main/java/com/minhtetthar/post_now/controller/NotificationController.java) - Notification endpoints
- [FollowController.java](src/main/java/com/minhtetthar/post_now/controller/FollowController.java) - Follow endpoints
