package eulerity.taskmanager.ai;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eulerity.taskmanager.exception.AiServiceException;
import org.junit.jupiter.api.Test;

/**
 * Verifies the offline guard: with no real API key configured, the generator
 * fails fast with a clean {@link AiServiceException} (which the handler maps to
 * 503) instead of attempting a network call. No live key, no network.
 */
class ClaudeTaskBreakdownGeneratorTest {

    @Test
    void generate_throwsAiServiceException_whenKeyBlank() {
        ClaudeTaskBreakdownGenerator generator = new ClaudeTaskBreakdownGenerator("");

        assertThatThrownBy(() -> generator.generate("Write report", "Q2 numbers"))
                .isInstanceOf(AiServiceException.class)
                .hasMessageContaining("not configured");
    }

    @Test
    void generate_throwsAiServiceException_whenKeyIsDummyPlaceholder() {
        ClaudeTaskBreakdownGenerator generator = new ClaudeTaskBreakdownGenerator("dummy");

        assertThatThrownBy(() -> generator.generate("Write report", null))
                .isInstanceOf(AiServiceException.class);
    }
}
