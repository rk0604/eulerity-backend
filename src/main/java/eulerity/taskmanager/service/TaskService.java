package eulerity.taskmanager.service;

import eulerity.taskmanager.ai.GeneratedSubtask;
import eulerity.taskmanager.ai.TaskBreakdownGenerator;
import eulerity.taskmanager.dto.SubtaskResponse;
import eulerity.taskmanager.dto.TaskBreakdownResponse;
import eulerity.taskmanager.dto.TaskRequest;
import eulerity.taskmanager.dto.TaskResponse;
import eulerity.taskmanager.enums.Priority;
import eulerity.taskmanager.enums.Status;
import eulerity.taskmanager.exception.ResourceNotFoundException;
import eulerity.taskmanager.mapper.TaskMapper;
import eulerity.taskmanager.model.Task;
import eulerity.taskmanager.repository.TaskRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Concrete business-logic layer for Tasks. Single implementation by design —
 * no interface/impl split, since there are no competing runtime strategies.
 */
@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final TaskMapper taskMapper;
    private final TaskBreakdownGenerator breakdownGenerator;

    public TaskService(TaskRepository taskRepository, TaskMapper taskMapper,
                       TaskBreakdownGenerator breakdownGenerator) {
        this.taskRepository = taskRepository;
        this.taskMapper = taskMapper;
        this.breakdownGenerator = breakdownGenerator;
    }

    /** Create a task, applying sensible defaults when priority/status are omitted. */
    public TaskResponse createTask(TaskRequest request) {
        Task task = taskMapper.toEntity(request);
        applyDefaults(task);
        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    /** Return every task as a response DTO. */
    @Transactional(readOnly = true)
    public List<TaskResponse> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(taskMapper::toResponse)
                .toList();
    }

    /** Return one task by id, or throw if it doesn't exist. */
    @Transactional(readOnly = true)
    public TaskResponse getTaskById(Long id) {
        Task task = findTaskOrThrow(id);
        return taskMapper.toResponse(task);
    }

    /** Apply in-place updates to an existing task, preserving its identity. */
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = findTaskOrThrow(id);
        taskMapper.updateEntity(task, request);
        applyDefaults(task);
        Task saved = taskRepository.save(task);
        return taskMapper.toResponse(saved);
    }

    /** Delete a task by id, or throw if it doesn't exist. */
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    /**
     * Break a task into AI-suggested subtasks. Stateless: the suggestions are
     * returned to the caller and never persisted.
     */
    @Transactional(readOnly = true)
    public TaskBreakdownResponse breakdownTask(Long id) {
        Task task = findTaskOrThrow(id);
        List<GeneratedSubtask> generated =
                breakdownGenerator.generate(task.getTitle(), task.getDescription());
        List<SubtaskResponse> subtasks = generated.stream()
                .map(s -> SubtaskResponse.builder()
                        .title(s.title())
                        .description(s.description())
                        .build())
                .toList();
        return TaskBreakdownResponse.builder()
                .taskId(task.getId())
                .title(task.getTitle())
                .subtasks(subtasks)
                .build();
    }

    // --- helpers ---

    private Task findTaskOrThrow(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
    }

    private void applyDefaults(Task task) {
        if (task.getStatus() == null) {
            task.setStatus(Status.TODO);
        }
        if (task.getPriority() == null) {
            task.setPriority(Priority.MEDIUM);
        }
    }
}
