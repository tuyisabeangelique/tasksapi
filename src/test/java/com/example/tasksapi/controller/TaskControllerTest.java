package com.example.tasksapi.controller;

import com.example.tasksapi.model.Task;
import com.example.tasksapi.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskController taskController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(taskController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getAllTasks_ShouldReturnAllTasks() throws Exception {
        // Given
        Task task1 = new Task();
        task1.setId(1L);
        task1.setTitle("Task 1");
        task1.setDescription("Description 1");
        task1.setCompleted(false);

        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setDescription("Description 2");
        task2.setCompleted(true);

        List<Task> tasks = Arrays.asList(task1, task2);
        when(taskRepository.findAll()).thenReturn(tasks);

        // When & Then
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].title").value("Task 2"));

        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void createTask_ShouldReturnCreatedTask() throws Exception {
        // Given
        Task taskToCreate = new Task();
        taskToCreate.setTitle("New Task");
        taskToCreate.setDescription("New Description");
        taskToCreate.setCompleted(false);

        Task createdTask = new Task();
        createdTask.setId(1L);
        createdTask.setTitle("New Task");
        createdTask.setDescription("New Description");
        createdTask.setCompleted(false);

        when(taskRepository.save(any(Task.class))).thenReturn(createdTask);

        // When & Then
        mockMvc.perform(post("/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(taskToCreate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.description").value("New Description"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getTaskById_ShouldReturnTask() throws Exception {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Task 1");
        task.setDescription("Description 1");
        task.setCompleted(false);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        // When & Then
        mockMvc.perform(get("/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Task 1"))
                .andExpect(jsonPath("$.description").value("Description 1"))
                .andExpect(jsonPath("$.completed").value(false));

        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void getTaskById_WhenTaskNotFound_ShouldReturnNotFound() throws Exception {
        // Given
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/tasks/999"))
                .andExpect(status().isNotFound());

        verify(taskRepository, times(1)).findById(999L);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void updateTask_ShouldReturnUpdatedTask() throws Exception {
        // Given
        Task existingTask = new Task();
        existingTask.setId(1L);
        existingTask.setTitle("Old Title");
        existingTask.setDescription("Old Description");
        existingTask.setCompleted(false);

        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Updated Title");
        updatedTask.setDescription("Updated Description");
        updatedTask.setCompleted(true);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(existingTask));
        when(taskRepository.save(any(Task.class))).thenReturn(updatedTask);

        // When & Then
        mockMvc.perform(put("/tasks/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedTask)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.description").value("Updated Description"))
                .andExpect(jsonPath("$.completed").value(true));

        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteTask_ShouldDeleteTask() throws Exception {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setCompleted(false);

        when(taskRepository.existsById(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isOk());

        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    @WithMockUser(roles = "MEMBER")
    void deleteTask_WhenNotAdmin_ShouldBeForbidden() throws Exception {
        // Given
        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setCompleted(false);

        when(taskRepository.existsById(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/tasks/1"))
                .andExpect(status().isForbidden());

        verify(taskRepository, never()).deleteById(any());
    }
} 