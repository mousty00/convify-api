package com.mousty.convify_api.controller;

import com.mousty.convify_api.dto.request.ConvertRequest;
import com.mousty.convify_api.dto.request.FilepathRequest;
import com.mousty.convify_api.model.ConversionJob;
import com.mousty.convify_api.service.ConversionManagerService;
import com.mousty.convify_api.service.FileStorageService;
import io.github.bucket4j.Bucket;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "YouTube Converter", description = "API for converting YouTube videos")
public class YouTubeController {

    private static final Logger log = LoggerFactory.getLogger(YouTubeController.class);

    private final ConversionManagerService conversionManager;
    private final FileStorageService storageService;
    private final Bucket rateLimitBucket;

    public YouTubeController(ConversionManagerService conversionManager,
                             FileStorageService storageService,
                             Bucket rateLimitBucket) {
        this.conversionManager = conversionManager;
        this.storageService = storageService;
        this.rateLimitBucket = rateLimitBucket;
    }

    @PostMapping("/convert/async")
    @Operation(summary = "Start asynchronous video conversion")
    public ResponseEntity<?> convertAsync(@Valid @RequestBody ConvertRequest request) {
        if (!rateLimitBucket.tryConsume(1)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("error", "Rate limit exceeded"));
        }

        String jobId = conversionManager.startConversion(request.url(), request.format());

        return ResponseEntity.accepted().body(Map.of(
                "jobId", jobId,
                "status", "pending",
                "message", "Conversion started. Check status at /convert/status/" + jobId
        ));
    }

    @GetMapping("/convert/status/{jobId}")
    @Operation(summary = "Get conversion job status")
    public ResponseEntity<ConversionJob> getStatus(@PathVariable String jobId) {
        return ResponseEntity.ok(conversionManager.getStatus(jobId));
    }

    @PostMapping("/download")
    @Operation(summary = "Download converted file")
    public ResponseEntity<Resource> download(@Valid @RequestBody FilepathRequest request) {
        try {
            Path filePath = storageService.validatePath(request.filepath());
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = request.filepath().toLowerCase().endsWith(".mp3")
                    ? "audio/mpeg" : "video/mp4";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filePath.getFileName() + "\"")
                    .body(resource);
        } catch (SecurityException e) {
            log.warn("Security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}