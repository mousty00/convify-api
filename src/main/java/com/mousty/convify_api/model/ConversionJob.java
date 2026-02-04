package com.mousty.convify_api.model;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ConversionJob {
    private final String jobId;
    private final String url;
    private final String format;
    private ConversionStatus status;
    private String videoId;
    private String videoTitle;
    private String filePath;
    private String errorMessage;
    private final Instant createdAt;
    private Instant completedAt;

    public ConversionJob(String jobId, String url, String format) {
        this.jobId = jobId;
        this.url = url;
        this.format = format;
        this.status = ConversionStatus.PENDING;
        this.createdAt = Instant.now();
    }
}
