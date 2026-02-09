# Generated API Types and Routes Reference

This document describes the TypeScript types and API routes that will be generated from your OpenAPI spec.

## 📚 Table of Contents

1. [Models (DTOs)](#models)
2. [API Routes](#api-routes)
3. [Usage Examples](#usage-examples)

---

## Models

All models are automatically generated in `src/generated/api/models/`.

### Auth Models

#### `LoginRequest`
```typescript
interface LoginRequest {
  username: string;      // Required
  password: string;      // Required
}
```

#### `AuthResponse`
```typescript
interface AuthResponse {
  token: string;         // JWT token
  username: string;
  email: string;
  role: 'USER' | 'ADMIN';
  message?: string;
}
```

---

### User Models

#### `UserDto`
```typescript
interface UserDto {
  id: number;
  username: string;
  email: string;
  firstName?: string;
  lastName?: string;
  bio?: string;
  profileImage?: string;
  role: 'USER' | 'ADMIN';
  createdAt: string;     // ISO 8601 datetime
  updatedAt: string;
}
```

#### `UserCreateDto`
```typescript
interface UserCreateDto {
  username: string;          // 3-50 chars, required
  email: string;             // Valid email, required
  password: string;          // 6-100 chars, required
  firstName?: string;
  lastName?: string;
  bio?: string;
  profileImage?: string;
}
```

#### `UserUpdateDto`
```typescript
interface UserUpdateDto {
  firstName?: string;
  lastName?: string;
  bio?: string;
  profileImage?: string;
}
```

#### `UserSummaryDto`
```typescript
interface UserSummaryDto {
  id: number;
  username: string;
  profileImage?: string;
}
```

---

### Post Models

#### `PostDto`
```typescript
interface PostDto {
  id: number;
  title: string;
  content?: string;
  type: 'PUBLIC' | 'PRIVATE' | 'DRAFT';
  imageUrl?: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  author: UserSummaryDto;
  likesCount: number;
  commentsCount: number;
  likedByCurrentUser?: boolean;
}
```

#### `PostCreateDto`
```typescript
interface PostCreateDto {
  title: string;                           // Max 1000 chars, required
  content?: string;
  type?: 'PUBLIC' | 'PRIVATE' | 'DRAFT'; // Default: PUBLIC
  imageUrl?: string;
}
```

#### `PostUpdateDto`
```typescript
interface PostUpdateDto {
  title?: string;
  content?: string;
  type?: 'PUBLIC' | 'PRIVATE' | 'DRAFT';
  imageUrl?: string;
  active?: boolean;
}
```

---

### Comment Models

#### `CommentDto`
```typescript
interface CommentDto {
  id: number;
  content: string;
  active: boolean;
  createdAt: string;
  updatedAt: string;
  author: UserSummaryDto;
  postId: number;
}
```

#### `CommentCreateDto`
```typescript
interface CommentCreateDto {
  content: string;  // Required
}
```

---

### Notification Models

#### `NotificationDto`
```typescript
interface NotificationDto {
  id: number;
  type: 'NEW_POST' | 'NEW_COMMENT' | 'NEW_LIKE' | 'NEW_FOLLOW';
  actor: UserSummaryDto;
  postId?: number;
  commentId?: number;
  read: boolean;
  createdAt: string;
  message: string;
}
```

---

### Follow Models

#### `FollowDto`
```typescript
interface FollowDto {
  id: number;
  follower: UserSummaryDto;
  following: UserSummaryDto;
  createdAt: string;
}
```

---

## API Routes

All routes are auto-generated in `src/generated/api/apis/`.

### PostsApi

```typescript
// Get all posts (paginated)
getAllPosts(page?: number, size?: number): Promise<PostDto[]>;

// Get post by ID
getPostById(id: number): Promise<PostDto>;

// Search posts
searchPosts(query: string, page?: number, size?: number): Promise<PostDto[]>;

// Get posts by author
getPostsByAuthor(username: string, page?: number, size?: number): Promise<PostDto[]>;

// Create post (Auth required)
createPost(body: PostCreateDto): Promise<PostDto>;

// Create post with image (Auth required)
createPostWithImage(formData: FormData): Promise<PostDto>;

// Update post (Auth required)
updatePost(id: number, body: PostUpdateDto): Promise<PostDto>;

// Update post image (Auth required)
updatePostImage(id: number, formData: FormData): Promise<PostDto>;

// Delete post (Auth required)
deletePost(id: number): Promise<void>;

// Delete post image (Auth required)
deletePostImage(id: number): Promise<PostDto>;
```

### UsersApi

```typescript
// Get current user (Auth required)
getCurrentUser(): Promise<UserDto>;

// Get user by username
getUserByUsername(username: string): Promise<UserDto>;

// List all users
getAllUsers(page?: number, size?: number): Promise<UserDto[]>;

// Search users
searchUsers(query: string, page?: number, size?: number): Promise<UserDto[]>;

// Update current user (Auth required)
updateCurrentUser(body: UserUpdateDto): Promise<UserDto>;

// Delete current user (Auth required)
deleteCurrentUser(): Promise<void>;

// Upload profile image (Auth required)
uploadProfileImage(formData: FormData): Promise<UserDto>;

// Delete profile image (Auth required)
deleteProfileImage(): Promise<UserDto>;

// Get followers
getFollowers(username: string, page?: number, size?: number): Promise<UserSummaryDto[]>;

// Get following
getFollowing(username: string, page?: number, size?: number): Promise<UserSummaryDto[]>;

// Get follow stats
getFollowStats(username: string): Promise<{
  followers: number;
  following: number;
  isFollowing?: boolean;
}>;

// Follow user (Auth required)
followUser(username: string): Promise<FollowDto>;

// Unfollow user (Auth required)
unfollowUser(username: string): Promise<void>;
```

### CommentsApi

```typescript
// Get comments for post
getCommentsForPost(postId: number, page?: number, size?: number): Promise<CommentDto[]>;

// Get comments by user
getCommentsByUser(username: string, page?: number, size?: number): Promise<CommentDto[]>;

// Create comment (Auth required)
createComment(postId: number, body: CommentCreateDto): Promise<CommentDto>;

// Delete comment (Auth required)
deleteComment(id: number): Promise<void>;
```

### LikesApi

```typescript
// Get like count for post
getLikesCount(postId: number): Promise<{ count: number }>;

// Check if post is liked by current user (Auth required)
isPostLikedByUser(postId: number): Promise<{ liked: boolean }>;

// Like post (Auth required)
likePost(postId: number): Promise<{ message: string }>;

// Unlike post (Auth required)
unlikePost(postId: number): Promise<{ message: string }>;

// Toggle like (Auth required)
toggleLike(postId: number): Promise<{ message: string }>;
```

### NotificationApi

```typescript
// Get all notifications (Auth required)
getAllNotifications(page?: number, size?: number): Promise<NotificationDto[]>;

// Get unread notifications (Auth required)
getUnreadNotifications(page?: number, size?: number): Promise<NotificationDto[]>;

// Get unread count (Auth required)
getUnreadCount(): Promise<{ count: number }>;

// Mark notification as read (Auth required)
markAsRead(id: number): Promise<void>;

// Mark all notifications as read (Auth required)
markAllAsRead(): Promise<void>;
```

### AuthApi

```typescript
// Register new user
register(body: UserCreateDto): Promise<AuthResponse>;

// Login
login(body: LoginRequest): Promise<AuthResponse>;

// Validate token (Auth optional)
validateToken(): Promise<{ valid: boolean; username: string }>;
```

---

## Usage Examples

### Login and Store Token

```typescript
import { apiClient, setAuthToken } from '@/api/client';

const handleLogin = async (username: string, password: string) => {
  try {
    const response = await apiClient.auth.login({
      username,
      password,
    });
    
    // Store token for future requests
    setAuthToken(response.token);
    
    // Now all future requests will include the token
    return response;
  } catch (error) {
    console.error('Login failed:', error);
  }
};
```

### Create a Post

```typescript
import { apiClient } from '@/api/client';
import type { PostCreateDto } from '@/generated/api';

const createNewPost = async () => {
  try {
    const postData: PostCreateDto = {
      title: 'My First Post',
      content: 'This is my first post using type-safe API client!',
      type: 'PUBLIC',
    };
    
    const newPost = await apiClient.posts.createPost(postData);
    console.log('Post created:', newPost);
  } catch (error) {
    console.error('Failed to create post:', error);
  }
};
```

### Fetch Posts with Pagination

```typescript
import { apiClient } from '@/api/client';
import type { PostDto } from '@/generated/api';

const fetchPosts = async (page: number, size: number = 20) => {
  try {
    // Full TypeScript autocompletion on all parameters ✨
    const posts: PostDto[] = await apiClient.posts.getAllPosts(page, size);
    return posts;
  } catch (error) {
    console.error('Failed to fetch posts:', error);
    return [];
  }
};
```

### Upload Profile Image

```typescript
import { apiClient } from '@/api/client';

const uploadProfileImage = async (imageFile: File) => {
  try {
    const formData = new FormData();
    formData.append('image', imageFile);
    
    const updatedUser = await apiClient.users.uploadProfileImage(formData);
    console.log('Profile updated:', updatedUser);
  } catch (error) {
    console.error('Upload failed:', error);
  }
};
```

### React Hook with API

```typescript
import { useEffect, useState } from 'react';
import { apiClient } from '@/api/client';
import type { PostDto } from '@/generated/api';

export const useUserPosts = (username: string) => {
  const [posts, setPosts] = useState<PostDto[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchPosts = async () => {
      try {
        setLoading(true);
        const data = await apiClient.posts.getPostsByAuthor(username);
        setPosts(data);
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to fetch posts');
      } finally {
        setLoading(false);
      }
    };

    fetchPosts();
  }, [username]);

  return { posts, loading, error };
};
```

### Error Handling

```typescript
import { apiClient } from '@/api/client';
import type { ApiError } from '@/generated/api';

const handleError = async () => {
  try {
    await apiClient.posts.getPostById(999); // Non-existent post
  } catch (error) {
    if (error instanceof Response) {
      console.log('Status:', error.status);
      console.log('StatusText:', error.statusText);
      
      if (error.status === 404) {
        console.log('Post not found');
      } else if (error.status === 401) {
        console.log('Unauthorized - need to login');
      }
    }
  }
};
```

---

## 🔄 Workflow

1. **Backend changes** → Restart backend
2. **Run**: `npm run generate-api`
3. **New types** → Generated in `src/generated/api/`
4. **TypeScript errors** → Update your code to match new API
5. **Build & deploy** → No runtime type errors!

---

## 📖 Resources

- [Generated REST API Specification](http://localhost:8090/v3/api-docs)
- [Swagger UI](http://localhost:8090/swagger-ui.html)
- [OpenAPI Generator Documentation](https://openapi-generator.tech/)

