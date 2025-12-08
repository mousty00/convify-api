package com.mousty.convify_api.exception;

import com.mousty.convify_api.dto.response.ErrorMessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.io.FileNotFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles IllegalArgumentException (400 BAD REQUEST).
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleValidationExceptions(IllegalArgumentException ex) {
        final HttpStatus status = HttpStatus.BAD_REQUEST;

        ErrorMessageResponse error = new ErrorMessageResponse(
                "validation_error",
                ex.getMessage(),
                status.value()
        );
        return new ResponseEntity<>(error, status);
    }

    /**
     * Handles FileNotFoundException (404 NOT FOUND).
     */
    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<?> handleFileNotFoundException() {
        final HttpStatus status = HttpStatus.NOT_FOUND;

        ErrorMessageResponse error = new ErrorMessageResponse(
                "not_found",
                "The requested file was not found.",
                status.value()
        );
        return new ResponseEntity<>(error, status);
    }

    /**
     * Handles all other uncaught Exception types (500 INTERNAL SERVER ERROR).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllUncaughtException(Exception ex, WebRequest request) {
        final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = ex.getMessage();

        String finalMessage = request.getDescription(false).contains("/convert")
                ? "Error during video conversion: " + message
                : "An unexpected error occurred: " + message;

        ErrorMessageResponse error = new ErrorMessageResponse(
                "internal_server_error",
                finalMessage,
                status.value()
        );

        return new ResponseEntity<>(error, status);
    }
}