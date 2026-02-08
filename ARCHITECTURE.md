# PostNow - Application Architecture

## Overview
PostNow is a Spring Boot-based social media application with a clean, layered architecture following best practices and SOLID principles.

## Architecture Layers

```
┌─────────────────────────────────────┐
│         Controllers                  │  ← REST API Endpoints
├─────────────────────────────────────┤
│         Services                     │  ← Business Logic
├─────────────────────────────────────┤
│         Repositories                 │  ← Data Access Layer
├─────────────────────────────────────┤
│         Entities                     │  ← Database Models
└─────────────────────────────────────┘

         ↕ (Mappers)
    
┌─────────────────────────────────────┐
│         DTOs                         │  ← Data Transfer Objects
└─────────────────────────────────────┘
```

---

## Project Structure

```
src/main/java/com/minhtetthar/post_now/
├── config/                     # Configuration classes
│   ├── JwtAuthFilter.java     # JWT authentication filter
│   ├── JwtService.java        # JWT token generation/validation
│   └── SecurityConfig.java    # Spring Security configuration
│
├── controller/                 # REST Controllers (separated by domain)
│   ├── AuthController.java    # Authentication endpoints
│   ├── UserController.java    # User management endpoints
│   ├── PostController.java    # Post management endpoints
│   ├── CommentController.java # Comment management endpoints
│   ├── LikeController.java    # Like management endpoints
│   └── HealthController.java  # Health check endpoint
│
├── dto/                        # Data Transfer Objects
│   ├── auth/
│   │   ├── LoginRequestDto.java
│   │   └── AuthResponseDto.java
│   ├── user/
│   │   ├── UserDto.java
│   │   ├── UserSummaryDto.java
│   │   ├── UserCreateDto.java
│   │   └── UserUpdateDto.java
│   ├── post/
│   │   ├── PostDto.java
│   │   ├── PostCreateDto.java
│   │   └── PostUpdateDto.java
│   └── comment/
│       ├── CommentDto.java
│       └── CommentCreateDto.java
│
├── entity/                     # JPA Entities (Database Models)
│   ├── User.java
│   ├── Post.java
│   ├── Comment.java
│   └── Like.java
│
├── mapper/                     # MapStruct Mappers
│   ├── UserMapper.java
│   ├── PostMapper.java
│   └── CommentMapper.java
│
├── repository/                 # Spring Data JPA Repositories
│   ├── UserRepository.java
│   ├── PostRepository.java
│   ├── CommentRepository.java
│   └── LikeRepository.java
│
├── service/                    # Business Logic Services
│   ├── AuthService.java
│   ├── UserService.java
│   ├── PostService.java
│   ├── CommentService.java
│   └── LikeService.java
│
└── PostNowApplication.java    # Main application class
```

---

## Key Components

### 1. **Entities**
JPA entities representing database tables with relationships:
- **User**: User accounts with authentication details
- **Post**: User-generated posts with content
- **Comment**: Comments on posts
- **Like**: Like relationships between users and posts

### 2. **DTOs (Data Transfer Objects)**
Separate DTOs for different purposes:
- **Request DTOs**: For creating/updating resources (with validation)
- **Response DTOs**: For returning data to clients
- **Summary DTOs**: Lightweight versions for nested data

### 3. **Mappers**
MapStruct-based mappers for entity ↔ DTO conversion:
- Automatic mapping based on field names
- Custom mapping rules for complex scenarios
- Handles nested relationships

### 4. **Repositories**
Spring Data JPA repositories with custom queries:
- Standard CRUD operations
- Custom finder methods
- Complex queries with @Query annotation
- Pagination support

### 5. **Services**
Business logic layer with transactional support:
- Orchestrates repository calls
- Implements business rules
- Handles authorization checks
- Enriches DTOs with calculated data

### 6. **Controllers**
RESTful API endpoints separated by domain:
- **AuthController**: Registration, login, token validation
- **UserController**: User profile management
- **PostController**: Post CRUD operations
- **CommentController**: Comment management
- **LikeController**: Like/unlike operations

---

## Design Patterns Used

### 1. **Repository Pattern**
- Abstracts data access logic
- Uses Spring Data JPA for implementation
- Custom queries when needed

### 2. **Service Layer Pattern**
- Separates business logic from controllers
- Transactional boundaries
- Reusable business operations

### 3. **DTO Pattern**
- Decouples API contracts from domain models
- Prevents over-fetching/under-fetching
- Enables versioning

### 4. **Mapper Pattern**
- Automated entity-DTO conversion
- Centralized mapping logic
- Type-safe conversions

### 5. **Builder Pattern**
- Used in entities and DTOs (via Lombok)
- Fluent API for object creation
- Immutable object support

---

## Best Practices Implemented

### 1. **Separation of Concerns**
✅ Controllers handle HTTP requests/responses only  
✅ Services contain business logic  
✅ Repositories handle data access  
✅ DTOs separate API contract from domain model

### 2. **Single Responsibility Principle**
✅ Each controller handles one domain (Post, Comment, Like, etc.)  
✅ Each service has a single responsibility  
✅ Each DTO serves one specific purpose

### 3. **Dependency Injection**
✅ Constructor injection (via Lombok @RequiredArgsConstructor)  
✅ Field injection avoided  
✅ Testable components

### 4. **Validation**
✅ Input validation with Jakarta Validation (@Valid, @NotBlank, etc.)  
✅ Business rule validation in services  
✅ Custom validators where needed

### 5. **Security**
✅ JWT-based authentication  
✅ Password encryption (BCrypt)  
✅ Role-based authorization  
✅ Stateless sessions

### 6. **Database Design**
✅ Proper relationships (One-to-Many, Many-to-One)  
✅ Soft deletes (active flags)  
✅ Timestamps for audit trail  
✅ Unique constraints where needed

### 7. **Pagination**
✅ All list endpoints support pagination  
✅ Prevents performance issues with large datasets  
✅ Configurable page size and sorting

### 8. **Error Handling**
✅ Meaningful HTTP status codes  
✅ Consistent error responses  
✅ Exception handling in services

---

## Technology Stack

- **Java 21**: Latest LTS version
- **Spring Boot 3.5.10**: Framework
- **Spring Data JPA**: Database access
- **Spring Security**: Authentication/Authorization
- **PostgreSQL**: Production database
- **H2**: Development/test database
- **Lombok**: Boilerplate reduction
- **MapStruct**: Object mapping
- **JWT (jjwt)**: Token-based auth
- **Hibernate**: ORM implementation

---

## Running the Application

### Development Mode (with H2)
```bash
./mvnw spring-boot:run
```

### Production Mode (with PostgreSQL)
1. Configure `application.properties` with PostgreSQL settings
2. Run:
```bash
./mvnw clean package
java -jar target/post-now-0.0.1-SNAPSHOT.jar
```

### Using Docker Compose
```bash
docker-compose up -d
```

---

## API Documentation

See [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for complete API reference.

## Database Schema

See [DATABASE_SCHEMA.md](DATABASE_SCHEMA.md) for database design documentation.

---

## Testing

### Unit Tests
Test services with mocked repositories:
```bash
./mvnw test
```

### Integration Tests
Test full stack with test database:
```bash
./mvnw verify
```

---

## Future Enhancements

- [ ] Add global exception handler (@ControllerAdvice)
- [ ] Implement custom exceptions for better error messages
- [ ] Add API versioning (/api/v1/...)
- [ ] Implement rate limiting
- [ ] Add Swagger/OpenAPI documentation
- [ ] Implement WebSocket for real-time notifications
- [ ] Add file upload for images
- [ ] Implement follow/follower relationships
- [ ] Add feed algorithm
- [ ] Implement notifications system
- [ ] Add search with Elasticsearch
- [ ] Implement caching with Redis
- [ ] Add comprehensive test coverage
- [ ] Set up CI/CD pipeline

---

## Contributing

1. Follow the existing architecture patterns
2. Create separate controllers for new domains
3. Use DTOs for all API contracts
4. Implement proper validation
5. Write tests for new features
6. Update documentation

---

## License

This project is licensed under the MIT License.