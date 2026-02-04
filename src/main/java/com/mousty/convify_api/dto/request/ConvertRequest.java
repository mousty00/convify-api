package com.mousty.convify_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request to convert a YouTube video")
public record ConvertRequest(
        
        @NotBlank(message = "URL cannot be empty")
        @Pattern(
            regexp = "^https?://(www\\.)?(youtube\\.com|youtu\\.be)/.*",
            message = "Invalid YouTube URL format"
        )
        @Schema(description = "YouTube video URL", 
                example = "https://www.youtube.com/watch?v=dQw4w9WgXcQ")
        String url,
        
        @NotBlank(message = "Format cannot be empty")
        @Pattern(
            regexp = "^(mp3|mp4)$",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Format must be either 'mp3' or 'mp4'"
        )
        @Schema(description = "Output format", example = "mp3", allowableValues = {"mp3", "mp4"})
        String format
) {}
