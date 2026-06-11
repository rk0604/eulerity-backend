package eulerity.taskmanager.dto;

import eulerity.taskmanager.enums.Priority;
import eulerity.taskmanager.enums.Status;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Outbound API contract returned to clients. Includes the generated {@code id}
 * but exposes only the fields we choose to publish, keeping the entity's
 * persistence details out of the HTTP layer.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {

    private Long id;

    private String title;

    private String description;

    private LocalDate dueDate;

    private Priority priority;

    private Status status;
}
