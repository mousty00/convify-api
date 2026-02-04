package com.mousty.convify_api.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class VideoDownloadService {
    private static final Logger log = LoggerFactory.getLogger(VideoDownloadService.class);
    private final Semaphore semaphore;

    public VideoDownloadService(@Value("${youtube.download.max-concurrent:3}") int max) {
        this.semaphore = new Semaphore(max);
    }

    @PostConstruct
    public void verifyTools() throws Exception {
        runCommand(List.of("yt-dlp", "--version"));
        runCommand(List.of("ffmpeg", "-version"));
    }

    public Path download(String url, String format, String title, Path dir) throws Exception {
        if (!semaphore.tryAcquire(30, TimeUnit.SECONDS)) throw new Exception("Server busy");
        try {
            Path target = dir.resolve(title + "." + format);
            List<String> cmd = buildCommand(url, format, target.toString());
            runCommand(cmd);
            if (!Files.exists(target)) throw new IOException("Download failed");
            return target;
        } finally {
            semaphore.release();
        }
    }

    private List<String> buildCommand(String url, String format, String output) {
        List<String> cmd = new ArrayList<>(List.of("yt-dlp", "--no-check-certificate", "-o", output));
        if ("mp3".equalsIgnoreCase(format)) {
            cmd.addAll(List.of("--extract-audio", "--audio-format", "mp3", "--audio-quality", "0"));
        } else {
            cmd.addAll(List.of("-f", "bestvideo[ext=mp4]+bestaudio[ext=m4a]/best[ext=mp4]", "--merge-output-format", "mp4"));
        }
        cmd.add(url);
        return cmd;
    }

    private void runCommand(List<String> command) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(command).start();
        if (process.waitFor() != 0) {
            throw new IOException("Command failed: " + String.join(" ", command));
        }
    }
}