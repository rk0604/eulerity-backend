package eulerity.taskmanager.ai;

import java.util.List;

/**
 * Abstraction over the AI provider that turns a task into subtask suggestions.
 * Keeps the service layer decoupled from the Anthropic SDK and trivially
 * mockable in tests.
 */
public interface TaskBreakdownGenerator {

    /**
     * Generate actionable subtasks for a task.
     *
     * @param title       the task title (required)
     * @param description the task description (may be null)
     * @return ordered subtask suggestions
     */
    List<GeneratedSubtask> generate(String title, String description);
}
