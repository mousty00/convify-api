package com.mousty.convify_api.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.VideoListResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class YouTubeMetadataService {
    private static final Logger log = LoggerFactory.getLogger(YouTubeMetadataService.class);

    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.application-name:Convify}")
    private String applicationName;

    private static final Pattern[] VIDEO_ID_PATTERNS = {
            Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([^&\\?/]+)"),
            Pattern.compile("youtube\\.com/embed/([^&\\?/]+)"),
            Pattern.compile("youtube\\.com/shorts/([^&\\?/]+)")
    };

    public String extractVideoId(String url) {
        if (url == null) return null;
        for (Pattern pattern : VIDEO_ID_PATTERNS) {
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) return matcher.group(1);
        }
        return null;
    }

    @Cacheable(value = "videoTitles", key = "#videoId")
    public String fetchVideoTitle(String videoId) throws Exception {
        YouTube youtube = new YouTube.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                request -> {})
                .setApplicationName(applicationName)
                .build();

        VideoListResponse response = youtube.videos()
                .list(Collections.singletonList("snippet"))
                .setKey(apiKey)
                .setId(Collections.singletonList(videoId))
                .execute();

        if (response.getItems() == null || response.getItems().isEmpty()) {
            throw new Exception("Video not found: " + videoId);
        }

        return sanitizeFilename(response.getItems().getFirst().getSnippet().getTitle());
    }

    private String sanitizeFilename(String filename) {
        return filename.replaceAll("[\\\\/:*?\"<>|]", "-")
                .replaceAll("[^a-zA-Z0-9.\\-_ ]", "-")
                .substring(0, Math.min(filename.length(), 200));
    }
}