package com.mousty.convify_api.controller;

import com.mousty.convify_api.dto.request.ConvertRequest;
import com.mousty.convify_api.dto.request.FilepathRequest;
import com.mousty.convify_api.service.YouTubeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class YouTubeController {

    private final YouTubeService youTubeService;

    @Autowired
    public YouTubeController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    @PostMapping("/convert")
    public ResponseEntity<?> convert(@RequestBody ConvertRequest request) throws Exception {
        return ResponseEntity.ok().body(
                youTubeService.convertAndPrepareResponse(
                        request.url(),
                        request.format()
                )
        );
    }


    @PostMapping("/download")
    public ResponseEntity<?> download(@RequestBody FilepathRequest request) throws Exception {

        Map<String, Object> downloadData = youTubeService.getDownloadData(request.filepath());

        Resource resource = (Resource) downloadData.get("resource");
        String filename = (String) downloadData.get("filename");
        String contentType = (String) downloadData.get("contentType");

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
