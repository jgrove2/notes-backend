package dev.jgrove2.notes_backend.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
public class S3Config {

    @Value("${r2.access.key.id}")
    private String accessKeyId;

    @Value("${r2.secret.access.key}")
    private String secretAccessKey;

    @Value("${r2.account.id}")
    private String accountId;

    @Value("${r2.endpoint}")
    private String endpoint;

    @Value("${r2.bucket.name}")
    private String bucketName;

    @Bean
    public S3Client s3Client() {
        AwsBasicCredentials awsCredentials = AwsBasicCredentials.create(accessKeyId, secretAccessKey);

        return S3Client.builder()
                .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                .region(Region.US_EAST_1) // R2 doesn't use regions, but SDK requires it
                .endpointOverride(URI.create(endpoint))
                .forcePathStyle(true) // Required for R2
                .build();
    }

    @Bean
    public String bucketName() {
        return bucketName;
    }
}