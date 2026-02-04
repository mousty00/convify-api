package com.mousty.convify_api.service;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class FileStorageService {
    private static final Logger log = LoggerFactory.getLogger(FileStorageService.class);
    @Getter
    private final Path downloadFolder;

    @Value("${youtube.download.min-disk-space-gb:1}")
    private long minDiskSpaceGb;

    @Value("${youtube.download.file-retention-hours:24}")
    private long fileRetentionHours;

    public FileStorageService(@Value("${app.download.dir:/tmp/yt-downloads}") String dir) throws IOException {
        this.downloadFolder = Paths.get(dir).toAbsolutePath().normalize();
        if (!Files.exists(downloadFolder)) Files.createDirectories(downloadFolder);
    }

    public void checkDiskSpace() throws IOException {
        if (downloadFolder.toFile().getUsableSpace() < minDiskSpaceGb * 1_000_000_000L) {
            throw new IOException("Insufficient disk space.");
        }
    }

    @Scheduled(cron = "${youtube.download.cleanup-cron:0 0 2 * * *}")
    public void cleanupOldFiles() throws IOException {
        Instant cutoff = Instant.now().minus(fileRetentionHours, ChronoUnit.HOURS);
        try (var stream = Files.walk(downloadFolder)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> {
                        try { return Files.getLastModifiedTime(p).toInstant().isBefore(cutoff); }
                        catch (IOException e) { return false; }
                    })
                    .forEach(p -> {
                        try { Files.delete(p); } catch (IOException ignored) {}
                    });
        }
    }

    public Path validatePath(String filepath) {
        Path path = Paths.get(filepath).normalize();
        if (!path.startsWith(downloadFolder)) throw new SecurityException("Invalid path");
        return path;
    }
}