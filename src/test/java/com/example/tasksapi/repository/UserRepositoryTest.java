package com.example.tasksapi.repository;

import com.example.tasksapi.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Create test users
        user1 = new User();
        user1.setUsername("testuser1");
        user1.setEmail("test1@example.com");
        user1.setPassword("encodedpassword1");

        user2 = new User();
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2.setPassword("encodedpassword2");

        // Clear any existing data
        entityManager.clear();
    }

    @Test
    void save_ShouldPersistUser() {
        // When
        User savedUser = userRepository.save(user1);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getUsername()).isEqualTo("testuser1");
        assertThat(savedUser.getEmail()).isEqualTo("test1@example.com");

        // Verify it's actually persisted in the database
        User foundUser = entityManager.find(User.class, savedUser.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getUsername()).isEqualTo("testuser1");
    }

    @Test
    void findByUsername_ShouldReturnUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(user1);

        // When
        Optional<User> foundUser = userRepository.findByUsername("testuser1");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser1");
        assertThat(foundUser.get().getEmail()).isEqualTo("test1@example.com");
    }

    @Test
    void findByUsername_WhenUserNotFound_ShouldReturnEmpty() {
        // When
        Optional<User> foundUser = userRepository.findByUsername("nonexistent");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    void existsByUsername_ShouldReturnTrue_WhenUserExists() {
        // Given
        entityManager.persistAndFlush(user1);

        // When
        boolean exists = userRepository.existsByUsername("testuser1");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByUsername_ShouldReturnFalse_WhenUserDoesNotExist() {
        // When
        boolean exists = userRepository.existsByUsername("nonexistent");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Given
        entityManager.persistAndFlush(user1);

        // When
        boolean exists = userRepository.existsByEmail("test1@example.com");

        // Then
        assertThat(exists).isTrue();
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailDoesNotExist() {
        // When
        boolean exists = userRepository.existsByEmail("nonexistent@example.com");

        // Then
        assertThat(exists).isFalse();
    }

    @Test
    void save_ShouldUpdateExistingUser() {
        // Given
        User savedUser = entityManager.persistAndFlush(user1);
        savedUser.setEmail("updated@example.com");

        // When
        User updatedUser = userRepository.save(savedUser);

        // Then
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");

        // Verify it's actually updated in the database
        User foundUser = entityManager.find(User.class, savedUser.getId());
        assertThat(foundUser.getEmail()).isEqualTo("updated@example.com");
    }
} 