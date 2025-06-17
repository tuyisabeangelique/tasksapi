package com.example.tasksapi.repository;

import com.example.tasksapi.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    private Task task1;
    private Task task2;

    @BeforeEach
    void setUp() {
        // Create test tasks
        task1 = new Task();
        task1.setTitle("Test Task 1");
        task1.setDescription("Test Description 1");
        task1.setCompleted(false);

        task2 = new Task();
        task2.setTitle("Test Task 2");
        task2.setDescription("Test Description 2");
        task2.setCompleted(true);

        // Clear any existing data
        entityManager.clear();
    }

    @Test
    void save_ShouldPersistTask() {
        // When
        Task savedTask = taskRepository.save(task1);

        // Then
        assertThat(savedTask.getId()).isNotNull();
        assertThat(savedTask.getTitle()).isEqualTo("Test Task 1");
        assertThat(savedTask.getDescription()).isEqualTo("Test Description 1");
        assertThat(savedTask.isCompleted()).isFalse();

        // Verify it's actually persisted in the database
        Task foundTask = entityManager.find(Task.class, savedTask.getId());
        assertThat(foundTask).isNotNull();
        assertThat(foundTask.getTitle()).isEqualTo("Test Task 1");
    }

    @Test
    void findAll_ShouldReturnAllTasks() {
        // Given
        entityManager.persistAndFlush(task1);
        entityManager.persistAndFlush(task2);

        // When
        List<Task> tasks = taskRepository.findAll();

        // Then
        assertThat(tasks).hasSize(2);
        assertThat(tasks).extracting("title")
                .containsExactlyInAnyOrder("Test Task 1", "Test Task 2");
    }

    @Test
    void findById_ShouldReturnTask() {
        // Given
        Task savedTask = entityManager.persistAndFlush(task1);

        // When
        Optional<Task> foundTask = taskRepository.findById(savedTask.getId());

        // Then
        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getTitle()).isEqualTo("Test Task 1");
    }

    @Test
    void findById_WhenTaskNotFound_ShouldReturnEmpty() {
        // When
        Optional<Task> foundTask = taskRepository.findById(999L);

        // Then
        assertThat(foundTask).isEmpty();
    }

    @Test
    void save_ShouldUpdateExistingTask() {
        // Given
        Task savedTask = entityManager.persistAndFlush(task1);
        savedTask.setTitle("Updated Title");
        savedTask.setCompleted(true);

        // When
        Task updatedTask = taskRepository.save(savedTask);

        // Then
        assertThat(updatedTask.getId()).isEqualTo(savedTask.getId());
        assertThat(updatedTask.getTitle()).isEqualTo("Updated Title");
        assertThat(updatedTask.isCompleted()).isTrue();

        // Verify it's actually updated in the database
        Task foundTask = entityManager.find(Task.class, savedTask.getId());
        assertThat(foundTask.getTitle()).isEqualTo("Updated Title");
        assertThat(foundTask.isCompleted()).isTrue();
    }

    @Test
    void deleteById_ShouldRemoveTask() {
        // Given
        Task savedTask = entityManager.persistAndFlush(task1);

        // When
        taskRepository.deleteById(savedTask.getId());

        // Then
        Task foundTask = entityManager.find(Task.class, savedTask.getId());
        assertThat(foundTask).isNull();
    }
} 