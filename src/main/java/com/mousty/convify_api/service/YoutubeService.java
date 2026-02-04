package com.mousty.convify_api.service;

import com.mousty.convify_api.dto.request.ConvertRequest;
import com.mousty.convify_api.dto.request.FilepathRequest;
import com.mousty.convify_api.model.ConversionJob;
import io.github.bucket4j.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.Map;

@Service
public class YoutubeService {

    private final static Logger log = LoggerFactory.getLogger(YoutubeService.class);

    private final ConversionManagerService conversionManager;
    private final FileStorageService storageService;
    private final Bucket rateLimitBucket;

    public YoutubeService(
        ConversionManagerService conversionManager,
        FileStorageService storageService,
        Bucket rateLimitBucket
    ){
        this.conversionManager = conversionManager;
        this.storageService = storageService;
        this.rateLimitBucket = rateLimitBucket;
    }

    public ResponseEntity<?> convertAsync(ConvertRequest request) {
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

    public ResponseEntity<ConversionJob> getStatus(String jobId) {
        return ResponseEntity.ok(conversionManager.getStatus(jobId));
    }

    public ResponseEntity<Resource> download(FilepathRequest request) {
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

    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }
}
