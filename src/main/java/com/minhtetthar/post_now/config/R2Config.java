package com.minhtetthar.post_now.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@Slf4j
public class R2Config {

    @Value("${cloudflare.r2.access-key}")
    private String accessKey;

    @Value("${cloudflare.r2.secret-key}")
    private String secretKey;

    @Value("${cloudflare.r2.endpoint}")
    private String endpoint;

    @Value("${cloudflare.r2.bucket-name}")
    private String bucketName;

    @Value("${cloudflare.r2.public-url}")
    private String publicUrl;

    @Bean
    public S3Client s3Client() {
        log.info("Initializing S3Client with endpoint: {}", endpoint);
        log.info("Bucket name: {}", bucketName);
        log.info("Public URL: {}", publicUrl);

        if (accessKey == null || accessKey.isEmpty()) {
            throw new IllegalArgumentException("R2 access key is not configured");
        }
        if (secretKey == null || secretKey.isEmpty()) {
            throw new IllegalArgumentException("R2 secret key is not configured");
        }
        if (endpoint == null || endpoint.isEmpty()) {
            throw new IllegalArgumentException("R2 endpoint is not configured");
        }
        if (bucketName == null || bucketName.isEmpty()) {
            throw new IllegalArgumentException("R2 bucket name is not configured");
        }
        if (publicUrl == null || publicUrl.isEmpty()) {
            throw new IllegalArgumentException("R2 public URL is not configured");
        }

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.US_EAST_1) // R2 doesn't use regions, but SDK requires one
                .build();
    }

    public String getBucketName() {
        return bucketName;
    }

    public String getPublicUrl() {
        return publicUrl;
    }
}
