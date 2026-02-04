package com.mousty.convify_api.controller;

import com.mousty.convify_api.model.ConversionJob;
import com.mousty.convify_api.service.ConversionManagerService;
import com.mousty.convify_api.service.YoutubeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@Tag(name = "YouTube Converter Graphql", description = "GraphQL API for converting YouTube videos")
public class YoutubeController {

    private final YoutubeService service;
    private final ConversionManagerService conversionManager;

    public YoutubeController(
            YoutubeService service,
            ConversionManagerService conversionManager
            ) {
        this.service = service;
        this.conversionManager = conversionManager;
    }

    @QueryMapping(value = "conversionStatus")
    public ConversionJob getStatus(@Argument String jobId) {
        return conversionManager.getStatus(jobId);
    }
}
