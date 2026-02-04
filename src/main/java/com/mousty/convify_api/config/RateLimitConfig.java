package com.mousty.convify_api.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${youtube.rate-limit.capacity:10}")
    private int capacity;

    @Value("${youtube.rate-limit.refill-tokens:10}")
    private int refillTokens;

    @Value("${youtube.rate-limit.refill-duration-minutes:1}")
    private int refillDurationMinutes;

    /**
     * Create rate limiting bucket
     * Default: 10 requests per minute
     */
    @Bean
    public Bucket rateLimitBucket() {
        Bandwidth limit = Bandwidth.classic(
                capacity,
                Refill.intervally(refillTokens, Duration.ofMinutes(refillDurationMinutes))
        );
        
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
