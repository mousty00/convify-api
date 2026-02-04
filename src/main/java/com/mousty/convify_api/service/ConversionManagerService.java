package com.mousty.convify_api.service;

import com.mousty.convify_api.model.*;
import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.Timer;
import org.slf4j.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

@Service
public class ConversionManagerService {
    private static final Logger log = LoggerFactory.getLogger(ConversionManagerService.class);

    private final YouTubeMetadataService metadataService;
    private final VideoDownloadService downloadService;
    private final FileStorageService storageService;

    private final Map<String, ConversionJob> jobs = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newFixedThreadPool(5);

    private final Counter successCounter;
    private final Timer timer;

    public ConversionManagerService(YouTubeMetadataService ms, VideoDownloadService ds, FileStorageService fs, MeterRegistry reg) {
        this.metadataService = ms;
        this.downloadService = ds;
        this.storageService = fs;
        this.successCounter = reg.counter("conversion.success");
        this.timer = reg.timer("conversion.duration");
    }

    public String startConversion(String url, String format) {
        String jobId = UUID.randomUUID().toString();
        ConversionJob job = new ConversionJob(jobId, url, format);
        jobs.put(jobId, job);

        executor.submit(() -> processJob(job));
        return jobId;
    }

    private void processJob(ConversionJob job) {
        Timer.Sample sample = Timer.start();
        MDC.put("jobId", job.getJobId());
        try {
            job.setStatus(ConversionStatus.PROCESSING);
            String videoId = metadataService.extractVideoId(job.getUrl());
            String title = metadataService.fetchVideoTitle(videoId);

            storageService.checkDiskSpace();
            var path = downloadService.download(job.getUrl(), job.getFormat(), title, storageService.getDownloadFolder());

            job.setFilePath(path.toString());
            job.setVideoTitle(title);
            job.setStatus(ConversionStatus.COMPLETED);
            successCounter.increment();
        } catch (Exception e) {
            job.setStatus(ConversionStatus.FAILED);
            job.setErrorMessage(e.getMessage());
        } finally {
            job.setCompletedAt(Instant.now());
            sample.stop(timer);
            MDC.remove("jobId");
        }
    }

    public ConversionJob getStatus(String jobId) {
        return Optional.ofNullable(jobs.get(jobId))
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));
    }

    @Scheduled(fixedDelay = 3600000)
    public void cleanJobs() {
        jobs.entrySet().removeIf(e -> e.getValue().getCompletedAt() != null &&
                e.getValue().getCompletedAt().isBefore(Instant.now().minus(1, java.time.temporal.ChronoUnit.HOURS)));
    }
}