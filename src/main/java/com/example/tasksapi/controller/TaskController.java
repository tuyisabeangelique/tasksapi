package com.example.tasksapi.controller;

import com.example.tasksapi.model.Task;
import com.example.tasksapi.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tasks")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TaskController {
    private final TaskRepository repo;

    public TaskController(TaskRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public List<Task> getAllTasks() {
        return repo.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public Task createTask(@RequestBody Task task) {
        return repo.save(task);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return repo.findById(id)
                .map(task -> ResponseEntity.ok(task))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MEMBER') or hasRole('ADMIN')")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updated) {
        return repo.findById(id)
                .map(task -> {
                    task.setTitle(updated.getTitle());
                    task.setDescription(updated.getDescription());
                    task.setCompleted(updated.isCompleted());
                    return ResponseEntity.ok(repo.save(task));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}