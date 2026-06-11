package eulerity.taskmanager.exception;

/**
 * Thrown when the AI breakdown provider is unavailable or fails (missing API
 * key, network/API error, empty response). Mapped to HTTP 503.
 */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
