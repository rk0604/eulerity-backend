package eulerity.taskmanager.exception;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Centralized translation of exceptions into structured HTTP error responses.
 * Keeps controllers free of try/catch and guarantees one consistent error shape.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Missing resource -> 404 Not Found. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    /** Bean-validation failure on a @Valid request body -> 400 Bad Request. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            // First message wins if a field has multiple violations.
            fieldErrors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Validation failed", request, fieldErrors);
    }

    /** Unparseable/malformed JSON body (or invalid enum value) -> 400 Bad Request. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(
            HttpMessageNotReadableException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed JSON request body", request, null);
    }

    /** Wrong type for a path variable or query param (e.g. /tasks/abc) -> 400 Bad Request. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String required = ex.getRequiredType() == null ? "the expected type"
                : ex.getRequiredType().getSimpleName();
        String message = "Parameter '" + ex.getName() + "' must be of type " + required;
        return build(HttpStatus.BAD_REQUEST, message, request, null);
    }

    // --- shared builder so every error has the same shape ---

    private ResponseEntity<ErrorResponse> build(
            HttpStatus status, String message, HttpServletRequest request,
            Map<String, String> fieldErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }
}
