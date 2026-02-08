# PostNow - Quick Start Guide

## Prerequisites

- Java 21 or higher
- Maven 3.6+ (or use the included Maven Wrapper)
- Your favorite IDE (IntelliJ IDEA, Eclipse, or VS Code)
- Postman or similar tool for API testing (optional)

---

## Getting Started

### 1. Clone and Navigate
```bash
cd e:\projects\post-now
```

### 2. Build the Project
```bash
./mvnw clean package
```

### 3. Run the Application
```bash
./mvnw spring-boot:run
```

The application will start on `http://localhost:8090`

### 4. Access H2 Database Console (Development)
- URL: `http://localhost:8090/h2-console`
- JDBC URL: `jdbc:h2:mem:postnowdb`
- Username: `sa`
- Password: (leave empty)

---

## Testing the API

### Step 1: Register a New User

**POST** `http://localhost:8090/api/auth/register`

```json
{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123",
  "firstName": "Test",
  "lastName": "User"
}
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "testuser",
  "email": "test@example.com",
  "role": "USER",
  "message": "Registration successful"
}
```

Copy the `token` value for subsequent requests.

---

### Step 2: Login (if already registered)

**POST** `http://localhost:8090/api/auth/login`

```json
{
  "username": "testuser",
  "password": "password123"
}
```

---

### Step 3: Create a Post

**POST** `http://localhost:8090/api/posts`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
Content-Type: application/json
```

**Body:**
```json
{
  "title": "My First Post",
  "content": "This is my first post on PostNow!",
  "type": "PUBLIC"
}
```

**Response:**
```json
{
  "id": 1,
  "title": "My First Post",
  "content": "This is my first post on PostNow!",
  "type": "PUBLIC",
  "active": true,
  "createdAt": "2026-02-08T03:50:00",
  "author": {
    "id": 1,
    "username": "testuser",
    "firstName": "Test",
    "lastName": "User"
  },
  "likesCount": 0,
  "commentsCount": 0,
  "likedByCurrentUser": false
}
```

---

### Step 4: Get All Posts

**GET** `http://localhost:8090/api/posts`

No authentication required for public posts.

**Optional Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `sort`: Sort by field (e.g., `createdAt,desc`)

---

### Step 5: Add a Comment

**POST** `http://localhost:8090/api/comments/post/1`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

**Body:**
```json
{
  "content": "Great post! Thanks for sharing."
}
```

---

### Step 6: Like a Post

**POST** `http://localhost:8090/api/likes/post/1`

**Headers:**
```
Authorization: Bearer YOUR_TOKEN_HERE
```

---

### Step 7: Get Post with Updated Counts

**GET** `http://localhost:8090/api/posts/1`

Now you'll see updated `likesCount` and `commentsCount`.

---

## API Endpoints Overview

### Public Endpoints (No Auth Required)
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login
- `GET /api/auth/validate` - Validate token
- `GET /api/posts` - Get all posts
- `GET /api/posts/{id}` - Get post by ID
- `GET /api/health` - Health check

### Protected Endpoints (Auth Required)

#### Users
- `GET /api/users/me` - Get current user profile
- `PUT /api/users/me` - Update profile
- `GET /api/users/{username}` - Get user by username
- `GET /api/users/search?query=...` - Search users

#### Posts
- `POST /api/posts` - Create post
- `PUT /api/posts/{id}` - Update post
- `DELETE /api/posts/{id}` - Delete post
- `GET /api/posts/user/{username}` - Get posts by user
- `GET /api/posts/search?query=...` - Search posts

#### Comments
- `GET /api/comments/post/{postId}` - Get comments for post
- `POST /api/comments/post/{postId}` - Add comment
- `DELETE /api/comments/{commentId}` - Delete comment

#### Likes
- `GET /api/likes/post/{postId}/count` - Get like count
- `GET /api/likes/post/{postId}/status` - Check if liked
- `POST /api/likes/post/{postId}` - Like post
- `DELETE /api/likes/post/{postId}` - Unlike post
- `POST /api/likes/post/{postId}/toggle` - Toggle like

---

## Postman Collection

You can import this into Postman:

1. Create a new Environment in Postman
2. Add variables:
   - `baseUrl`: `http://localhost:8090`
   - `token`: (will be set automatically after login)

3. Use `{{baseUrl}}` and `{{token}}` in your requests

---

## Development Tips

### Enable Debug Logging
In `application.properties`:
```properties
logging.level.com.minhtetthar.post_now=DEBUG
```

### View SQL Queries
Already enabled by default:
```properties
spring.jpa.show-sql=true
```

### Hot Reload (Spring Boot DevTools)
Add to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

---

## Production Deployment

### 1. Update application.properties

Comment out H2 configuration and uncomment PostgreSQL:

```properties
# Production Database
spring.datasource.url=jdbc:postgresql://localhost:5432/postnow
spring.datasource.username=your_username
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=validate

# Change JWT secret!
jwt.secret=your-super-secure-secret-key-minimum-256-bits
```

### 2. Build JAR
```bash
./mvnw clean package -DskipTests
```

### 3. Run
```bash
java -jar target/post-now-0.0.1-SNAPSHOT.jar
```

---

## Docker Deployment

### 1. Use Docker Compose
```bash
docker-compose up -d
```

This will start:
- PostgreSQL on port 5432
- PostNow application on port 8090

---

## Common Issues

### Port Already in Use
Change port in `application.properties`:
```properties
server.port=9090
```

### Database Connection Issues
Check PostgreSQL is running:
```bash
docker ps
```

### JWT Token Invalid
- Check token hasn't expired (24 hours by default)
- Ensure token is in format: `Bearer <token>`
- Verify JWT secret matches between requests

---

## Next Steps

1. Read [ARCHITECTURE.md](ARCHITECTURE.md) for system design
2. Check [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete API reference
3. Review [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) for database structure
4. Start building your frontend!

---

## Support

For issues or questions:
1. Check the documentation files
2. Review the code comments
3. Look at the controller/service implementations for examples

Happy coding! 🚀