package com.ecom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class R2Config {

    @Value("${app.r2.access-key}")
    private String accessKey;

    @Value("${app.r2.secret-key}")
    private String secretKey;

    @Value("${app.r2.account-id}")
    private String accountId;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        
        return S3Client.builder()
                .endpointOverride(URI.create("https://" + accountId + ".r2.cloudflarestorage.com"))
                .region(Region.US_EAST_1) // R2 requires a region setting, though it ignores it. US_EAST_1 is standard.
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }
}