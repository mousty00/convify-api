package com.mousty.convify_api.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class YouTubeService {

    private static final Logger log = LoggerFactory.getLogger(YouTubeService.class);
    private final Path downloadFolder;

    @Value("${youtube.api.key}")
    private String apiKey;

    @Value("${youtube.api.application-name:Convify}")
    private String applicationName;

    private static final Pattern[] VIDEO_ID_PATTERNS = {
            Pattern.compile("(?:youtube\\.com/watch\\?v=|youtu\\.be/)([^&\\?/]+)"),
            Pattern.compile("youtube\\.com/embed/([^&\\?/]+)"),
            Pattern.compile("youtube\\.com/v/([^&\\?/]+)"),
            Pattern.compile("youtube\\.com/shorts/([^&\\?/]+)")
    };

    public YouTubeService(@Value("${app.download.dir:/tmp/yt-downloads}") String downloadDir) throws IOException {
        this.downloadFolder = Paths.get(downloadDir);

        if (!Files.exists(this.downloadFolder)) {
            Files.createDirectories(this.downloadFolder);
            log.info("Created download directory: {}", this.downloadFolder);
        }
    }

    @PostConstruct
    public void init() throws IOException {
        // Check if FFmpeg is installed
        try {
            runCommand("ffmpeg -version");
            log.info("FFmpeg is installed and accessible");
        } catch (Exception e) {
            log.error("FFmpeg is not installed or not accessible. Video conversion will fail.");
            log.error("Please install FFmpeg: sudo apt install ffmpeg");
        }

        // Check yt-dlp version
        try {
            String version = runCommand("yt-dlp --version");
            log.info("yt-dlp version: {}", version);
        } catch (Exception e) {
            log.error("yt-dlp is not installed or not accessible");
        }
    }

    public String runCommand(String command) throws IOException, InterruptedException {
        log.debug("Executing command: {}", command);

        Process process = new ProcessBuilder("/bin/bash", "-c", command).start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
             BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {

            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }

            StringBuilder errors = new StringBuilder();
            while ((line = errorReader.readLine()) != null) {
                errors.append(line).append("\n");
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                String errorMsg = errors.toString().trim();
                log.error("Command failed with exit code {}. Error output: {}", exitCode, errorMsg);
                throw new IOException("Command execution failed. Error: " + (errorMsg.isEmpty() ? "No error message provided." : errorMsg));
            }

            return output.toString().trim();
        }
    }

    /**
     * Extract video ID from various YouTube URL formats using regex patterns
     */
    public String extractVideoIdFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            log.warn("Empty or null URL provided");
            return null;
        }

        for (Pattern pattern : VIDEO_ID_PATTERNS) {
            Matcher matcher = pattern.matcher(url);
            if (matcher.find()) {
                String videoId = matcher.group(1);
                log.debug("Extracted video ID: {} from URL: {}", videoId, url);
                return videoId;
            }
        }

        log.warn("Video ID not found for URL: {}", url);
        return null;
    }

    /**
     * Extract video title using YouTube Data API v3
     * This replaces the yt-dlp command for getting video titles
     */
    public String extractVideoTitle(String url) throws Exception {
        try {
            String videoId = extractVideoIdFromUrl(url);
            if (videoId == null) {
                throw new IllegalArgumentException("Invalid YouTube URL format: " + url);
            }

            log.info("Fetching video title for video ID: {}", videoId);

            YouTube youtube = new YouTube.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    request -> {}
            )
                    .setApplicationName(applicationName)
                    .build();

            YouTube.Videos.List request = youtube.videos()
                    .list(Collections.singletonList("snippet"))
                    .setKey(apiKey)
                    .setId(Collections.singletonList(videoId));

            VideoListResponse response = request.execute();

            if (response.getItems() == null || response.getItems().isEmpty()) {
                log.error("Video not found for ID: {}", videoId);
                throw new Exception("Video not found: " + videoId);
            }

            Video video = response.getItems().get(0);
            String title = video.getSnippet().getTitle();

            String sanitizedTitle = title.replaceAll("[^a-zA-Z0-9.\\- ]", "-").trim();

            log.info("Successfully extracted title: {}", sanitizedTitle);
            return sanitizedTitle;

        } catch (GeneralSecurityException | IOException e) {
            log.error("Failed to extract video title from: {}", url, e);
            throw new Exception("Could not retrieve video title: " + e.getMessage(), e);
        }
    }

    private String extractCommand(String format, String outputTemplate, String url) {
        String baseFlags = "--no-check-certificate " + "--extractor-args \"youtube:player_client=web\" ";

        return switch (format.toLowerCase()) {
            case "mp3" -> String.format("yt-dlp %s -x --audio-format mp3 --audio-quality 0 -o \"%s\" \"%s\"",
                    baseFlags, outputTemplate, url);
            case "mp4" -> {
                String formatSelector = "\"best[ext=mp4][height<=720]/bestvideo[ext=mp4][height<=720]+bestaudio[ext=m4a]/best\"";
                yield String.format("yt-dlp %s -f %s --merge-output-format mp4 -o \"%s\" \"%s\"",
                        baseFlags, formatSelector, outputTemplate, url);
            }
            default -> "";
        };
    }

    public Path convertVideo(String url, String format, String title) throws Exception {
        String outputFileName = title + "." + format;
        Path filePath = this.downloadFolder.resolve(outputFileName);
        String outputTemplate = filePath.toString();

        String commandExtract = extractCommand(format, outputTemplate, url);
        if (commandExtract.isEmpty()) {
            throw new IllegalArgumentException("Invalid format specified. Must be 'mp3' or 'mp4'.");
        }

        runCommand(commandExtract);

        if (Files.exists(filePath)) {
            log.info("Conversion successful. File: {}", filePath);
            return filePath;
        } else {
            log.error("File not found after conversion: {}", filePath);
            throw new IOException("File not found after conversion. Conversion tool might have failed silently.");
        }
    }

    public Map<String, String> convertAndPrepareResponse(String url, String format) throws Exception {

        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty or null.");
        }
        if (format == null || format.trim().isEmpty()) {
            throw new IllegalArgumentException("Format cannot be empty or null.");
        }
        if (!format.equalsIgnoreCase("mp3") && !format.equalsIgnoreCase("mp4")) {
            throw new IllegalArgumentException("Invalid format specified. Must be 'mp3' or 'mp4'.");
        }

        String videoId = extractVideoIdFromUrl(url);
        if (videoId == null) {
            throw new IllegalArgumentException("Invalid YouTube URL provided.");
        }

        String videoTitle = extractVideoTitle(url);

        Path filePath = convertVideo(url, format, videoTitle);

        return Map.of(
                "status", "success",
                "message", "Video conversion completed successfully!",
                "videoId", videoId,
                "videoTitle", videoTitle,
                "filePath", filePath.toString()
        );
    }

    public Map<String, Object> getDownloadData(String filepath) throws IOException {

        if (filepath == null || filepath.trim().isEmpty()) {
            throw new IllegalArgumentException("Filepath cannot be empty.");
        }

        Path path = Paths.get(filepath);

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            log.warn("Attempted download of non-existent file: {}", filepath);
            throw new FileNotFoundException("File not found or is not a file: " + filepath);
        }

        Resource resource = new FileSystemResource(path.toFile());
        String filename = resource.getFilename();

        String contentType = filename.toLowerCase().endsWith(".mp3") ? "audio/mpeg" : "video/mp4";

        return Map.of(
                "resource", resource,
                "filename", filename,
                "contentType", contentType
        );
    }
}