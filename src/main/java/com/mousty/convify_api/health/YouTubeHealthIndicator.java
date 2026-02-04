package com.mousty.convify_api.health;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;

@Component
public class YouTubeHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(YouTubeHealthIndicator.class);

    @Override
    public Health health() {
        try {
            // Check yt-dlp is available
            ProcessBuilder pb = new ProcessBuilder("yt-dlp", "--version");
            Process process = pb.start();
            
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String version = reader.readLine();
                int exitCode = process.waitFor();
                
                if (exitCode == 0) {
                    return Health.up()
                            .withDetail("yt-dlp", "available")
                            .withDetail("version", version)
                            .build();
                } else {
                    return Health.down()
                            .withDetail("yt-dlp", "not available")
                            .build();
                }
            }
        } catch (Exception e) {
            log.error("Health check failed", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
