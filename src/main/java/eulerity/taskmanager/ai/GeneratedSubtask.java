package eulerity.taskmanager.ai;

/**
 * A single AI-generated subtask suggestion. Plain value type, decoupled from
 * both the JPA entity and the HTTP DTOs.
 */
public record GeneratedSubtask(String title, String description) {
}
