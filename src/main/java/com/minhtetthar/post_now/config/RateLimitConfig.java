package com.minhtetthar.post_now.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
@Slf4j
public class RateLimitConfig {

    /**
     * Rate limit tiers:
     * - STRICT: 10 requests per minute (login, registration)
     * - MODERATE: 30 requests per minute (post creation, follow/unfollow)
     * - LENIENT: 60 requests per minute (read operations, notifications)
     */

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(
                "rate-limit-buckets",  // Rate limiting cache with 1 hour TTL
                "users",               // User cache with 15 min TTL
                "posts",               // Posts cache with 15 min TTL
                "postStats",           // Post stats cache with 15 min TTL
                "relationships"        // Relationships cache with 15 min TTL
        );
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS)
                .maximumSize(100_000));
        return cacheManager;
    }

    /**
     * Creates a bucket for strict rate limiting (10 requests/minute)
     * Used for: login, registration, password reset
     */
    public Bucket createStrictBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(10)
                .refillIntervally(10, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Creates a bucket for moderate rate limiting (30 requests/minute)
     * Used for: post creation, comments, likes, follow/unfollow
     */
    public Bucket createModerateBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(30)
                .refillIntervally(30, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Creates a bucket for lenient rate limiting (60 requests/minute)
     * Used for: reading posts, notifications, user profiles
     */
    public Bucket createLenientBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(60)
                .refillIntervally(60, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Creates a bucket for authenticated users (higher limits)
     * 100 requests per minute for authenticated requests
     */
    public Bucket createAuthenticatedBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(100)
                .refillIntervally(100, Duration.ofMinutes(1))
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
