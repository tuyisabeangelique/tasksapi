# Task Management API

A robust RESTful API for task management built with Spring Boot, featuring JWT authentication, role-based access control, PostgreSQL database, and comprehensive testing.

## Features

- **JWT Authentication** - Secure user registration and login
- **Role-Based Access Control** - Admin and Member roles with different permissions
- **Task Management** - Full CRUD operations for tasks
- **PostgreSQL Database** - Reliable data persistence
- **Comprehensive Testing** - Unit tests, integration tests, and repository tests
- **Docker Support** - Containerized deployment

## 🛠️ Tech Stack

- **Backend**: Spring Boot 3.5.0
- **Database**: PostgreSQL 15
- **Security**: Spring Security + JWT
- **Testing**: JUnit 5 + Mockito
- **Containerization**: Docker + Docker Compose
- **Build Tool**: Maven
- **Java Version**: 17

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 15 (or Docker)
- Docker & Docker Compose (optional)

## Quick Start

### Option 1: Using Docker 

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd tasksapi
   ```

2. **Start the application with Docker Compose**
   ```bash
   docker compose up -d
   ```

3. **Access the API**
   - API Base URL: `http://localhost:8080`
   - Database: PostgreSQL on port 5432

### Option 2: Local Development

1. **Set up PostgreSQL database**
   ```sql
   CREATE DATABASE taskhub;
   CREATE USER taskhub_user WITH PASSWORD 'password123';
   GRANT ALL PRIVILEGES ON DATABASE taskhub TO taskhub_user;
   ```

2. **Update application.properties** (if needed)
   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/taskhub
   spring.datasource.username=taskhub_user
   spring.datasource.password=password123
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

## API Documentation

### Authentication Endpoints

#### Register User
```http
POST /api/auth/signup
Content-Type: application/json

{
  "username": "yourname",
  "email": "your@email.com",
  "password": "yourpassword"
}
```

#### Login User
```http
POST /api/auth/signin
Content-Type: application/json

{
  "username": "yourname",
  "password": "yourpassword"
}
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9...",
  "id": 1,
  "username": "yourname",
  "email": "your@email.com",
  "role": "ROLE_MEMBER",
  "tokenType": "Bearer"
}
```

#### Get Current User
```http
GET /api/auth/me
Authorization: Bearer <your-jwt-token>
```

### Task Management Endpoints

#### Get All Tasks
```http
GET /api/tasks
Authorization: Bearer <your-jwt-token>
```

#### Create Task
```http
POST /api/tasks
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "title": "Complete project",
  "description": "Finish the Spring Boot API",
  "completed": false
}
```

#### Get Task by ID
```http
GET /api/tasks/{id}
Authorization: Bearer <your-jwt-token>
```

#### Update Task
```http
PUT /api/tasks/{id}
Authorization: Bearer <your-jwt-token>
Content-Type: application/json

{
  "title": "Updated task title",
  "description": "Updated description",
  "completed": true
}
```

#### Delete Task
```http
DELETE /api/tasks/{id}
Authorization: Bearer <your-jwt-token>
```
*Note: Only users with ADMIN role can delete tasks*

### Test Endpoints

#### Test Public Access
```http
GET /api/test/public
```

#### Test Member Access
```http
GET /api/test/member
Authorization: Bearer <your-jwt-token>
```

#### Test Admin Access
```http
GET /api/test/admin
Authorization: Bearer <your-jwt-token>
```

## Authentication & Authorization

### JWT Token Usage
Include the JWT token in the Authorization header for protected endpoints:
```
Authorization: Bearer <your-jwt-token>
```

### User Roles
- **ROLE_MEMBER**: Can create, read, and update tasks
- **ROLE_ADMIN**: Can perform all operations including task deletion

### Security Features
- Password hashing with BCrypt
- JWT token expiration (24 hours by default)
- Role-based endpoint protection
- CORS configuration for frontend integration

## Testing

### Run All Tests
```bash
./mvnw test
```

### Test Categories
- **Unit Tests**: Controller, Service, and Utility classes
- **Integration Tests**: API endpoint testing
- **Repository Tests**: Database operation testing
- **Security Tests**: JWT and authentication testing

### Test Configuration
- Uses H2 in-memory database for testing
- Separate test properties in `application-test.properties`
- Mocked external dependencies

## Docker

### Build Image
```bash
docker build -t tasksapi .
```

### Run with Docker Compose
```bash
# Start all services
docker compose up -d

# View logs
docker compose logs app

# Stop services
docker compose down
```

### Docker Services
- **app**: Spring Boot application (port 8080)
- **postgres**: PostgreSQL database (port 5432)


## Configuration

### Database Schema
- **users**: User accounts with roles
- **task**: Task management data

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Ensure all tests pass
6. Submit a pull request

## Support

For issues and questions:
1. Check the existing issues
2. Create a new issue with detailed information
3. Include logs and error messages

## Future Enhancements

- [ ] Add task categories/tags
- [ ] Implement task search and filtering
- [ ] Add file upload for task attachments
- [ ] Implement task notifications
- [ ] Add user profile management
- [ ] Create React frontend
- [ ] Add API documentation with Swagger
- [ ] Implement rate limiting
- [ ] Add audit logging 
