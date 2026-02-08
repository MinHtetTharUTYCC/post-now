# API Documentation

## Overview
This API uses JWT token-based authentication. Include the token in the Authorization header as `Bearer <token>` for protected endpoints.

---

## Authentication Endpoints

### POST /api/auth/register
Register a new user.

**Request Body:**
```json
{
  "username": "string (3-50 chars, required)",
  "email": "string (valid email, required)",
  "password": "string (6-100 chars, required)",
  "firstName": "string (optional)",
  "lastName": "string (optional)",
  "bio": "string (optional)",
  "profileImage": "string (optional)"
}
```

**Response:** `201 Created`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "message": "Registration successful"
}
```

---

### POST /api/auth/login
Authenticate and receive JWT token.

**Request Body:**
```json
{
  "username": "string (required)",
  "password": "string (required)"
}
```

**Response:** `200 OK`
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "john_doe",
  "email": "john@example.com",
  "role": "USER",
  "message": "Login successful"
}
```

---

### GET /api/auth/validate
Validate JWT token.

**Headers:**
- `Authorization: Bearer <token>`

**Response:** `200 OK`
```json
{
  "valid": true,
  "username": "john_doe"
}
```

---

## User Endpoints

### GET /api/users/me
Get current user profile.

**Auth:** Required

**Response:** `200 OK`
```json
{
  "id": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "firstName": "John",
  "lastName": "Doe",
  "bio": "Software developer",
  "profileImage": "https://example.com/avatar.jpg",
  "role": "USER",
  "createdAt": "2026-01-01T10:00:00",
  "updatedAt": "2026-01-15T14:30:00"
}
```

---

### GET /api/users/{username}
Get user profile by username.

**Auth:** Optional

**Response:** `200 OK` (same structure as /me)

---

### GET /api/users
Get all users (paginated).

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `sort`: Sort field and direction (e.g., `username,asc`)

**Response:** `200 OK`
```json
{
  "content": [<UserDto>],
  "pageable": {...},
  "totalElements": 100,
  "totalPages": 5,
  "last": false,
  "size": 20,
  "number": 0
}
```

---

### GET /api/users/search
Search users.

**Query Parameters:**
- `query`: Search term (required)
- `page`, `size`, `sort`: Pagination parameters

**Response:** `200 OK` (paginated)

---

### PUT /api/users/me
Update current user profile.

**Auth:** Required

**Request Body:**
```json
{
  "email": "string (optional)",
  "firstName": "string (optional)",
  "lastName": "string (optional)",
  "bio": "string (max 1000 chars, optional)",
  "profileImage": "string (optional)"
}
```

**Response:** `200 OK` (UserDto)

---

### DELETE /api/users/me
Soft delete current user account.

**Auth:** Required

**Response:** `204 No Content`

---

## Post Endpoints

### GET /api/posts
Get all posts (paginated).

**Auth:** Optional (affects visibility)

**Query Parameters:**
- `page`, `size`, `sort`: Pagination parameters

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "title": "My First Post",
      "content": "This is the content...",
      "type": "PUBLIC",
      "imageUrl": "https://example.com/image.jpg",
      "active": true,
      "createdAt": "2026-02-01T10:00:00",
      "updatedAt": null,
      "author": {
        "id": 1,
        "username": "john_doe",
        "firstName": "John",
        "lastName": "Doe",
        "profileImage": "https://example.com/avatar.jpg"
      },
      "likesCount": 42,
      "commentsCount": 15,
      "likedByCurrentUser": true
    }
  ],
  "totalElements": 100,
  "totalPages": 5
}
```

---

### GET /api/posts/{id}
Get post by ID.

**Auth:** Optional

**Response:** `200 OK` (PostDto)

---

### GET /api/posts/search
Search posts.

**Query Parameters:**
- `query`: Search term (required)
- `page`, `size`, `sort`: Pagination parameters

**Auth:** Optional

**Response:** `200 OK` (paginated PostDto)

---

### GET /api/posts/user/{username}
Get posts by author.

**Query Parameters:**
- `page`, `size`, `sort`: Pagination parameters

**Auth:** Optional

**Response:** `200 OK` (paginated PostDto)

---

### POST /api/posts
Create a new post.

**Auth:** Required

**Request Body:**
```json
{
  "title": "string (max 1000 chars, required)",
  "content": "string (optional)",
  "type": "PUBLIC | PRIVATE | DRAFT (default: PUBLIC)",
  "imageUrl": "string (optional)"
}
```

**Response:** `201 Created` (PostDto)

---

### PUT /api/posts/{id}
Update a post.

**Auth:** Required (must be author)

**Request Body:**
```json
{
  "title": "string (optional)",
  "content": "string (optional)",
  "type": "PUBLIC | PRIVATE | DRAFT (optional)",
  "imageUrl": "string (optional)",
  "active": "boolean (optional)"
}
```

**Response:** `200 OK` (PostDto)

---

### DELETE /api/posts/{id}
Soft delete a post.

**Auth:** Required (must be author)

**Response:** `204 No Content`

---

## Comment Endpoints

### GET /api/comments/post/{postId}
Get comments for a post.

**Query Parameters:**
- `page`, `size`: Pagination parameters (default size: 50)

**Auth:** Optional

**Response:** `200 OK`
```json
{
  "content": [
    {
      "id": 1,
      "content": "Great post!",
      "active": true,
      "createdAt": "2026-02-01T11:00:00",
      "updatedAt": null,
      "author": {
        "id": 2,
        "username": "jane_smith",
        "firstName": "Jane",
        "lastName": "Smith",
        "profileImage": "https://example.com/jane.jpg"
      },
      "postId": 1
    }
  ],
  "totalElements": 15
}
```

---

### GET /api/comments/user/{username}
Get comments by a user.

**Query Parameters:**
- `page`, `size`, `sort`: Pagination parameters

**Auth:** Optional

**Response:** `200 OK` (paginated CommentDto)

---

### POST /api/comments/post/{postId}
Add a comment to a post.

**Auth:** Required

**Request Body:**
```json
{
  "content": "string (max 2000 chars, required)"
}
```

**Response:** `201 Created` (CommentDto)

---

### DELETE /api/comments/{commentId}
Soft delete a comment.

**Auth:** Required (must be author)

**Response:** `204 No Content`

---

## Like Endpoints

### GET /api/likes/post/{postId}/count
Get like count for a post.

**Auth:** Optional

**Response:** `200 OK`
```json
{
  "count": 42
}
```

---

### GET /api/likes/post/{postId}/status
Check if current user liked a post.

**Auth:** Required

**Response:** `200 OK`
```json
{
  "liked": true
}
```

---

### POST /api/likes/post/{postId}
Like a post.

**Auth:** Required

**Response:** `200 OK`
```json
{
  "message": "Post liked successfully"
}
```

---

### DELETE /api/likes/post/{postId}
Unlike a post.

**Auth:** Required

**Response:** `200 OK`
```json
{
  "message": "Post unliked successfully"
}
```

---

### POST /api/likes/post/{postId}/toggle
Toggle like status (like if not liked, unlike if already liked).

**Auth:** Required

**Response:** `200 OK`
```json
{
  "message": "Like toggled successfully"
}
```

---

## Health Check Endpoint

### GET /api/health
Check if API is running.

**Auth:** Not required

**Response:** `200 OK`
```json
{
  "status": "UP",
  "timestamp": "2026-02-08T10:00:00"
}
```

---

## Error Responses

All endpoints may return these error responses:

### 400 Bad Request
```json
{
  "error": "Invalid input",
  "details": "Validation errors..."
}
```

### 401 Unauthorized
```json
{
  "error": "Authentication required",
  "message": "Please provide a valid JWT token"
}
```

### 403 Forbidden
```json
{
  "error": "Access denied",
  "message": "You don't have permission to perform this action"
}
```

### 404 Not Found
```json
{
  "error": "Resource not found",
  "message": "The requested resource does not exist"
}
```

---

## Rate Limiting
Currently not implemented. Consider adding rate limiting in production.

## CORS
Configure CORS settings in SecurityConfig for your frontend domain.

## Versioning
API version: v1 (can be added to base path: `/api/v1/...`)