package com.mousty.convify_api.controller;

import com.mousty.convify_api.dto.request.ConvertRequest;
import com.mousty.convify_api.dto.request.FilepathRequest;
import com.mousty.convify_api.model.ConversionJob;
import com.mousty.convify_api.service.YoutubeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1")
@Tag(name = "YouTube Converter", description = "API for converting YouTube videos")
public class YouTubeRestController {

    private final YoutubeService service;


    public YouTubeRestController(YoutubeService service){
        this.service = service;
    }

    @PostMapping("/convert/async")
    @Operation(summary = "Start asynchronous video conversion")
    public ResponseEntity<?> convertAsync(@Valid @RequestBody ConvertRequest request) {
        return service.convertAsync(request);
    }

    @GetMapping("/convert/status/{jobId}")
    @Operation(summary = "Get conversion job status")
    public ResponseEntity<ConversionJob> getStatus(@PathVariable String jobId) {
        return service.getStatus(jobId);
    }

    @PostMapping("/download")
    @Operation(summary = "Download converted file")
    public ResponseEntity<Resource> download(@Valid @RequestBody FilepathRequest request) {
        return service.download(request);
    }

    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return service.health();
    }
}