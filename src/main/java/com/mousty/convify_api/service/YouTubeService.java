package com.mousty.convify_api.service;

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
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class YouTubeService {

    private static final Logger log = LoggerFactory.getLogger(YouTubeService.class);
    private final Path downloadFolder;

    public YouTubeService(@Value("${app.download.dir:/tmp/yt-downloads}") String downloadDir) throws IOException {
        this.downloadFolder = Paths.get(downloadDir);

        if (!Files.exists(this.downloadFolder)) {
            Files.createDirectories(this.downloadFolder);
            log.info("Created download directory: {}", this.downloadFolder);
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

    public String extractVideoIdFromUrl(String url) {
        if (url == null || url.trim().isEmpty()) return null;

        String regex = "^(?:https?://)?(?:www\\.)?(?:youtube\\.com/(?:(?:v|e(?:mbed)?)/|(?:[^/\\n]+\\S*?[?&]v=))|youtu\\.be/)([a-zA-Z0-9_-]{11})(?:[?&].*)?$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(url);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            log.warn("Video ID not found for URL: {}", url);
            return null;
        }
    }

    public String extractVideoTitle(String url) throws Exception {
        String command = String.format("yt-dlp --get-title \"%s\"", url);
        try {
            String title = runCommand(command).trim();
            return title.replaceAll("[^a-zA-Z0-9.\\- ]", "-").trim();
        } catch (IOException | InterruptedException e) {
            log.error("Failed to extract video title: {}", e.getMessage());
            throw new Exception("Could not retrieve video title. Check URL or yt-dlp installation.", e);
        }
    }

    private String extractCommand(String format, String outputTemplate, String url) {
        return switch (format.toLowerCase()) {
            case "mp3" -> String.format("yt-dlp -x --audio-format mp3 -o \"%s\" \"%s\"", outputTemplate, url);
            case "mp4" -> String.format("yt-dlp -f bestvideo[height<=720]+bestaudio --merge-output-format mp4 -o \"%s\" \"%s\"", outputTemplate, url);
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