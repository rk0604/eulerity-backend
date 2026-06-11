package eulerity.taskmanager.exception;

/**
 * Thrown when a requested entity (e.g. a Task) does not exist.
 * A future @RestControllerAdvice will translate this into an HTTP 404.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
