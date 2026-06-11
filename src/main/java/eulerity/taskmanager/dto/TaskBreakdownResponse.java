package eulerity.taskmanager.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Stateless response for an AI task breakdown: echoes which task was broken down
 * and the generated subtasks. None of this is persisted.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskBreakdownResponse {

    private Long taskId;

    private String title;

    private List<SubtaskResponse> subtasks;
}
