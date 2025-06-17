// Will handle CRUD operations for tasks by interacting with db

package com.example.tasksapi.repository;

import com.example.tasksapi.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;

// Each Task has a primary key of type Long
public interface TaskRepository extends JpaRepository<Task, Long> {

}