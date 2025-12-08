package com.mousty.convify_api.dto.response;

import java.time.LocalDateTime;

public record ErrorMessageResponse(
        String key,
        String message,
        int status,
        LocalDateTime timestamp
) {
    public ErrorMessageResponse(String key, String message, int status) {
        this(key, message, status, LocalDateTime.now());
    }
}