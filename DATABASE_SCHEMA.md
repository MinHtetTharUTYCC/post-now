# Database Schema Documentation

## Tables

### Users Table
```sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    bio VARCHAR(1000),
    profile_image VARCHAR(500),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    account_non_expired BOOLEAN DEFAULT true,
    account_non_locked BOOLEAN DEFAULT true,
    credentials_non_expired BOOLEAN DEFAULT true,
    enabled BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);
```

**Indexes:**
- `idx_users_username` on `username`
- `idx_users_email` on `email`
- `idx_users_enabled` on `enabled`

**Constraints:**
- Username must be unique
- Email must be unique
- Email must be valid format
- Password must be encrypted (BCrypt)
- Role must be one of: USER, ADMIN, MODERATOR

---

### Posts Table
```sql
CREATE TABLE posts (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(1000) NOT NULL,
    content TEXT,
    type VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    image_url VARCHAR(2000),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    author_id BIGINT NOT NULL,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Indexes:**
- `idx_posts_author_id` on `author_id`
- `idx_posts_created_at` on `created_at DESC`
- `idx_posts_active` on `active`
- `idx_posts_type` on `type`
- `idx_posts_title` on `title` (for search)

**Constraints:**
- Type must be one of: PUBLIC, PRIVATE, DRAFT
- Author must exist in users table
- Title is required

---

### Comments Table
```sql
CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    content VARCHAR(2000) NOT NULL,
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    post_id BIGINT NOT NULL,
    author_id BIGINT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE
);
```

**Indexes:**
- `idx_comments_post_id` on `post_id`
- `idx_comments_author_id` on `author_id`
- `idx_comments_created_at` on `created_at ASC`
- `idx_comments_active` on `active`

**Constraints:**
- Post must exist in posts table
- Author must exist in users table
- Content is required

---

### Likes Table
```sql
CREATE TABLE likes (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    FOREIGN KEY (post_id) REFERENCES posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (post_id, user_id)
);
```

**Indexes:**
- `idx_likes_post_id` on `post_id`
- `idx_likes_user_id` on `user_id`
- `idx_likes_post_user` on `(post_id, user_id)` UNIQUE

**Constraints:**
- Post must exist in posts table
- User must exist in users table
- A user can only like a post once (enforced by unique constraint)

---

## Relationships

### User → Posts (One-to-Many)
- One user can create many posts
- When a user is deleted, their posts are cascade deleted
- Posts are soft-deleted (active = false) by default

### User → Comments (One-to-Many)
- One user can create many comments
- When a user is deleted, their comments are cascade deleted
- Comments are soft-deleted (active = false) by default

### User → Likes (One-to-Many)
- One user can like many posts
- When a user is deleted, their likes are cascade deleted

### Post → Comments (One-to-Many)
- One post can have many comments
- When a post is deleted, its comments are cascade deleted

### Post → Likes (One-to-Many)
- One post can have many likes
- When a post is deleted, its likes are cascade deleted

---

## Business Rules

1. **User Registration:**
   - Username must be unique (3-50 characters)
   - Email must be unique and valid format
   - Password must be at least 6 characters (stored as BCrypt hash)
   - New users default to USER role

2. **Post Creation:**
   - Title is required (max 1000 characters)
   - Content is optional (no limit)
   - Posts default to PUBLIC type
   - Posts default to active = true

3. **Post Visibility:**
   - PUBLIC posts: Visible to everyone (authenticated and guests)
   - PRIVATE posts: Only visible to authenticated users
   - DRAFT posts: Only visible to the author

4. **Comments:**
   - Only authenticated users can comment
   - Comments can only be deleted by their author
   - Comments are soft-deleted (active = false)

5. **Likes:**
   - Only authenticated users can like posts
   - Users can only like a post once
   - Users can unlike posts they've previously liked

6. **Authentication:**
   - JWT token-based authentication
   - Tokens expire after configured time
   - Passwords are encrypted with BCrypt

---

## Query Patterns

### Frequently Used Queries

1. **Get all posts with counts:**
```java
// Returns posts with like count and comment count
// Filters by type based on authentication status
```

2. **Get posts by user:**
```java
// Returns all active posts by a specific author
// Ordered by created_at DESC
```

3. **Get comments for a post:**
```java
// Returns all active comments for a post
// Ordered by created_at ASC (oldest first)
```

4. **Check if user liked a post:**
```java
// Checks existence of like record for user and post
```

5. **Search posts:**
```java
// Searches in title and content
// Case-insensitive LIKE query
```

---

## Performance Considerations

1. **Indexes:** All foreign keys have indexes for fast joins
2. **Soft Deletes:** Uses `active` flag instead of hard deletes
3. **Pagination:** All list endpoints support pagination
4. **Lazy Loading:** Relationships use LAZY fetch to avoid N+1 queries
5. **Query Optimization:** Custom queries to fetch counts efficiently

---

## Migration Scripts

The schema is automatically created by JPA/Hibernate based on entity annotations.

For production, consider:
1. Using Flyway or Liquibase for version-controlled migrations
2. Adding database-specific optimizations
3. Creating materialized views for complex queries
4. Adding full-text search indexes for content search