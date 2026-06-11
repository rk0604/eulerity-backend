package eulerity.taskmanager.ai;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.StructuredMessageCreateParams;
import com.anthropic.models.messages.ThinkingConfigAdaptive;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import eulerity.taskmanager.exception.AiServiceException;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Anthropic-backed implementation of {@link TaskBreakdownGenerator}. Calls
 * Claude (claude-opus-4-8) with structured outputs so the model returns a typed
 * list of subtasks rather than free-form text we'd have to parse.
 */
@Component
public class ClaudeTaskBreakdownGenerator implements TaskBreakdownGenerator {

    private static final String MODEL = "claude-opus-4-8";

    private static final String SYSTEM_PROMPT = """
            You are a project-planning assistant. Given a task, break it into a small set of
            concrete, actionable subtasks. Return between 3 and 7 subtasks, ordered logically.
            Each subtask has a short imperative title and a single-sentence description.
            Do not repeat the original task as a subtask, and do not add commentary.""";

    private final String apiKey;

    public ClaudeTaskBreakdownGenerator(@Value("${anthropic.api.key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public List<GeneratedSubtask> generate(String title, String description) {
        if (apiKey == null || apiKey.isBlank() || "dummy".equalsIgnoreCase(apiKey.trim())) {
            throw new AiServiceException(
                    "Claude API key is not configured. Set 'anthropic.api.key' to enable task breakdown.");
        }

        AnthropicClient client = AnthropicOkHttpClient.builder().apiKey(apiKey).build();

        try {
            StructuredMessageCreateParams<BreakdownResult> params = MessageCreateParams.builder()
                    .model(MODEL)
                    .maxTokens(8192L)
                    .thinking(ThinkingConfigAdaptive.builder().build())
                    .system(SYSTEM_PROMPT)
                    .outputConfig(BreakdownResult.class)
                    .addUserMessage(buildPrompt(title, description))
                    .build();

            BreakdownResult result = client.messages().create(params).content().stream()
                    .flatMap(block -> block.text().stream())
                    .map(typed -> typed.text())
                    .findFirst()
                    .orElseThrow(() -> new AiServiceException("Claude returned no structured content."));

            return result.subtasks().stream()
                    .map(item -> new GeneratedSubtask(item.title(), item.description()))
                    .toList();
        } catch (AiServiceException e) {
            throw e;
        } catch (RuntimeException e) {
            // Wrap SDK/transport failures (auth, rate limit, network) into our 503 surface.
            throw new AiServiceException("Claude API call failed: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String title, String description) {
        String desc = (description == null || description.isBlank()) ? "(none provided)" : description;
        return "Break down the following task into actionable subtasks.\n\n"
                + "Title: " + title + "\n"
                + "Description: " + desc;
    }

    // --- structured-output schema (derived automatically by the SDK) ---

    record SubtaskItem(
            @JsonPropertyDescription("Short imperative subtask title, e.g. 'Draft outline'")
            String title,
            @JsonPropertyDescription("One-sentence description of what the subtask involves")
            String description) {
    }

    record BreakdownResult(List<SubtaskItem> subtasks) {
    }
}
