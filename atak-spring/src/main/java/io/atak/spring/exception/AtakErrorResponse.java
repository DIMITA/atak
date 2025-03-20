package io.atak.spring.exception;

import java.time.Instant;

/** Standard error payload returned by the ATAK exception handler. */
public record AtakErrorResponse(
        int status,
        String error,
        String message,
        String path,
        Instant timestamp
) {
    public static AtakErrorResponse of(int status, String error, String message, String path) {
        return new AtakErrorResponse(status, error, message, path, Instant.now());
    }
}
