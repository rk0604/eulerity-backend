package eulerity.taskmanager.mapper;

import eulerity.taskmanager.dto.TaskRequest;
import eulerity.taskmanager.dto.TaskResponse;
import eulerity.taskmanager.model.Task;
import org.springframework.stereotype.Component;

/**
 * Hand-written conversion between the {@link Task} entity and its DTOs.
 * Kept deliberately explicit (no reflection-based mapping library) so the
 * field-by-field contract is obvious and easy to audit.
 */
@Component
public class TaskMapper {

    /** Build a fresh entity from an inbound request (no id — it's DB-generated). */
    public Task toEntity(TaskRequest request) {
        if (request == null) {
            return null;
        }
        return Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .priority(request.getPriority())
                .status(request.getStatus())
                .build();
    }

    /** Map a persisted entity to the outbound response. */
    public TaskResponse toResponse(Task task) {
        if (task == null) {
            return null;
        }
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .dueDate(task.getDueDate())
                .priority(task.getPriority())
                .status(task.getStatus())
                .build();
    }

    /**
     * Copy mutable fields from a request onto an existing entity (for updates).
     * Leaves {@code id} untouched so the identity of the row is preserved.
     */
    public void updateEntity(Task target, TaskRequest request) {
        if (target == null || request == null) {
            return;
        }
        target.setTitle(request.getTitle());
        target.setDescription(request.getDescription());
        target.setDueDate(request.getDueDate());
        target.setPriority(request.getPriority());
        target.setStatus(request.getStatus());
    }
}
