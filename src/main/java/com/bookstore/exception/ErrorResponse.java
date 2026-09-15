package com.bookstore.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<FieldErrorItem> fieldErrors
) {
    public ErrorResponse(int status, String error, String message) {
        this(Instant.now(), status, error, message, null, null);
    }

    public ErrorResponse(int status, String error, String message, List<FieldErrorItem> fieldErrors) {
        this(Instant.now(), status, error, message, null, fieldErrors);
    }
}
