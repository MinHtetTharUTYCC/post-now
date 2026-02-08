package com.minhtetthar.post_now.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @GetMapping
    public ResponseEntity<?> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "PostNow API is running",
                "timestamp", LocalDateTime.now(),
                "service", "post-now",
                "version", "1.0.0"));
    }

    @GetMapping("/posts")
    public ResponseEntity<?> postsHealthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Posts service is healthy",
                "timestamp", LocalDateTime.now()));
    }

    @GetMapping("/auth")
    public ResponseEntity<?> authHealthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Authentication service is healthy",
                "timestamp", LocalDateTime.now()));
    }
}