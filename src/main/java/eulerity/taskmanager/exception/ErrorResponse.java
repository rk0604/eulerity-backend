package eulerity.taskmanager.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

/**
 * Structured error payload returned for every handled exception, so clients get
 * a consistent, machine-parseable shape. {@code fieldErrors} is only present on
 * validation failures (omitted from JSON when null).
 */
@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, String> fieldErrors;
}
