package eulerity.taskmanager.dto;

import eulerity.taskmanager.enums.Priority;
import eulerity.taskmanager.enums.Status;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Inbound API contract for creating/updating a Task.
 * Decoupled from the JPA entity: clients never see {@code id} and cannot
 * touch persistence concerns. Validation lives here, on the boundary.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "title is required")
    private String title;

    private String description;

    private LocalDate dueDate;

    // Optional on the wire; the service layer applies sensible defaults
    // (e.g. priority=MEDIUM, status=TODO) when these are null.
    private Priority priority;

    private Status status;
}
