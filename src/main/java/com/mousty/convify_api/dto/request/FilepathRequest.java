package com.mousty.convify_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to download a converted file")
public record FilepathRequest(

        @NotBlank(message = "Filepath cannot be empty")
        @Schema(description = "Path to the converted file",
                example = "/tmp/yt-downloads/video-title.mp3")
        String filepath
) {}
