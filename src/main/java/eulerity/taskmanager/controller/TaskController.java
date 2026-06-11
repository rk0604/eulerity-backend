package eulerity.taskmanager.controller;

import eulerity.taskmanager.dto.TaskRequest;
import eulerity.taskmanager.dto.TaskResponse;
import eulerity.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * REST endpoints for Tasks. Thin HTTP boundary: validates input, delegates to
 * the service, and translates results into proper status codes. No business
 * logic and no entity exposure — DTOs only.
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    /** Create a task. 201 Created with a Location header pointing at the new resource. */
    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @Valid @RequestBody TaskRequest request,
            UriComponentsBuilder uriBuilder) {
        TaskResponse created = taskService.createTask(request);
        URI location = uriBuilder.path("/tasks/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    /** List all tasks. */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }

    /** Fetch one task by id (404 if absent, via the exception handler). */
    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    /** Full update of an existing task (404 if absent). */
    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    /** Delete a task. 204 No Content (404 if absent). */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
