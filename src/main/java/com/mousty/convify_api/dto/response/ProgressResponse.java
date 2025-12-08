package com.mousty.convify_api.dto.response;

public record ProgressResponse(
        int progress,
        String status,
        String filePath
) {}