package com.mousty.convify_api.dto.response;

import lombok.Builder;

@Builder
public record ConvertResponse(
        String status,
        String message,
        String videoId,
        String videoTitle,
        String filePath
) {
}
