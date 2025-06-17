package com.example.tasksapi.integration;

import com.example.tasksapi.model.Task;
import com.example.tasksapi.model.User;
import com.example.tasksapi.payload.request.LoginRequest;
import com.example.tasksapi.payload.response.JwtResponse;
import com.example.tasksapi.repository.TaskRepository;
import com.example.tasksapi.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TaskApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private ObjectMapper objectMapper;
    private String baseUrl;
    private String jwtToken;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        baseUrl = "http://localhost:" + port;
        
        // Clear repositories
        taskRepository.deleteAll();
        userRepository.deleteAll();
        
        // Create test user and get JWT token
        createTestUserAndGetToken();
    }

    private void createTestUserAndGetToken() {
        // Create test user
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole(User.Role.ADMIN); // Set as ADMIN to allow delete operations
        userRepository.save(user);

        // Login to get JWT token
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password");

        ResponseEntity<JwtResponse> response = restTemplate.postForEntity(
                baseUrl + "/api/auth/signin",
                loginRequest,
                JwtResponse.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        jwtToken = response.getBody().getAccessToken();
    }

    @Test
    void getAllTasks_ShouldReturnTasks() {
        // Given
        Task task1 = new Task();
        task1.setTitle("Test Task 1");
        task1.setDescription("Description 1");
        task1.setCompleted(false);
        taskRepository.save(task1);

        Task task2 = new Task();
        task2.setTitle("Test Task 2");
        task2.setDescription("Description 2");
        task2.setCompleted(true);
        taskRepository.save(task2);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Task[]> response = restTemplate.exchange(
                baseUrl + "/tasks",
                HttpMethod.GET,
                entity,
                Task[].class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Task[] tasks = response.getBody();
        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting("title")
                .containsExactlyInAnyOrder("Test Task 1", "Test Task 2");
    }

    @Test
    void createTask_ShouldCreateNewTask() {
        // Given
        Task taskToCreate = new Task();
        taskToCreate.setTitle("New Task");
        taskToCreate.setDescription("New Description");
        taskToCreate.setCompleted(false);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Task> entity = new HttpEntity<>(taskToCreate, headers);

        ResponseEntity<Task> response = restTemplate.exchange(
                baseUrl + "/tasks",
                HttpMethod.POST,
                entity,
                Task.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Task createdTask = response.getBody();
        assertThat(createdTask.getId()).isNotNull();
        assertThat(createdTask.getTitle()).isEqualTo("New Task");
        assertThat(createdTask.getDescription()).isEqualTo("New Description");
        assertThat(createdTask.isCompleted()).isFalse();

        // Verify it's saved in the database
        List<Task> allTasks = taskRepository.findAll();
        assertThat(allTasks).hasSize(1);
        assertThat(allTasks.get(0).getTitle()).isEqualTo("New Task");
    }

    @Test
    void getTaskById_ShouldReturnTask() {
        // Given
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setCompleted(false);
        Task savedTask = taskRepository.save(task);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Task> response = restTemplate.exchange(
                baseUrl + "/tasks/" + savedTask.getId(),
                HttpMethod.GET,
                entity,
                Task.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Task foundTask = response.getBody();
        assertThat(foundTask.getId()).isEqualTo(savedTask.getId());
        assertThat(foundTask.getTitle()).isEqualTo("Test Task");
    }

    @Test
    void updateTask_ShouldUpdateTask() {
        // Given
        Task task = new Task();
        task.setTitle("Original Title");
        task.setDescription("Original Description");
        task.setCompleted(false);
        Task savedTask = taskRepository.save(task);

        Task updatedTask = new Task();
        updatedTask.setTitle("Updated Title");
        updatedTask.setDescription("Updated Description");
        updatedTask.setCompleted(true);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Task> entity = new HttpEntity<>(updatedTask, headers);

        ResponseEntity<Task> response = restTemplate.exchange(
                baseUrl + "/tasks/" + savedTask.getId(),
                HttpMethod.PUT,
                entity,
                Task.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Task resultTask = response.getBody();
        assertThat(resultTask.getId()).isEqualTo(savedTask.getId());
        assertThat(resultTask.getTitle()).isEqualTo("Updated Title");
        assertThat(resultTask.getDescription()).isEqualTo("Updated Description");
        assertThat(resultTask.isCompleted()).isTrue();
    }

    @Test
    void deleteTask_ShouldDeleteTask() {
        // Given
        Task task = new Task();
        task.setTitle("Task to Delete");
        task.setDescription("Description");
        task.setCompleted(false);
        Task savedTask = taskRepository.save(task);

        // When
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/tasks/" + savedTask.getId(),
                HttpMethod.DELETE,
                entity,
                Void.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        // Verify it's deleted from the database
        List<Task> allTasks = taskRepository.findAll();
        assertThat(allTasks).isEmpty();
    }

    @Test
    void accessWithoutToken_ShouldReturnUnauthorized() {
        // When
        ResponseEntity<String> response = restTemplate.getForEntity(
                baseUrl + "/tasks",
                String.class
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
} 