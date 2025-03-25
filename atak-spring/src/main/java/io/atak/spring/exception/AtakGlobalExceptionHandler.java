package io.atak.spring.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Centralised exception handler registered automatically by ATAK auto-configuration.
 *
 * <p>Handles:
 * <ul>
 *   <li>{@link RuntimeException} with "not found" message → 404</li>
 *   <li>{@link MethodArgumentNotValidException} → 400 with field errors</li>
 *   <li>Generic {@link Exception} → 500</li>
 * </ul>
 */
@RestControllerAdvice
public class AtakGlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<AtakErrorResponse> handleRuntime(RuntimeException ex, HttpServletRequest req) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Unexpected error";
        boolean isNotFound = msg.toLowerCase().contains("not found");
        HttpStatus status = isNotFound ? HttpStatus.NOT_FOUND : HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(AtakErrorResponse.of(status.value(), status.getReasonPhrase(), msg, req.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<AtakErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                               HttpServletRequest req) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest()
                .body(AtakErrorResponse.of(400, "Validation Failed", details, req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<AtakErrorResponse> handleGeneric(Exception ex, HttpServletRequest req) {
        return ResponseEntity.internalServerError()
                .body(AtakErrorResponse.of(500, "Internal Server Error",
                        ex.getMessage() != null ? ex.getMessage() : "Unknown error", req.getRequestURI()));
    }
}
