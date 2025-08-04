package dev.jgrove2.notes_backend.Services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Autowired
    private String bucketName;

    /**
     * Upload a file to R2/S3
     * 
     * @param fileInputStream The file input stream
     * @param fileName        The original file name
     * @param userId          The user ID for organization
     * @return The object key (path) in the bucket
     */
    public String uploadFile(InputStream fileInputStream, String fileName, Long userId) {
        try {
            // Generate a unique object key
            String objectKey = generateObjectKey(userId, fileName);

            // Upload the file
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType("application/json") // Assuming JSON files
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(fileInputStream, fileInputStream.available()));

            return objectKey;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to R2: " + e.getMessage(), e);
        }
    }

    /**
     * Update an existing file in R2/S3
     * 
     * @param fileInputStream The new file input stream
     * @param objectKey       The existing object key
     * @return The object key (same as input)
     */
    public String updateFile(InputStream fileInputStream, String objectKey) {
        try {
            // Upload the new file content
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(fileInputStream, fileInputStream.available()));

            return objectKey;
        } catch (IOException e) {
            throw new RuntimeException("Failed to update file in R2: " + e.getMessage(), e);
        }
    }

    /**
     * Delete a file from R2/S3
     * 
     * @param objectKey The object key to delete
     */
    public void deleteFile(String objectKey) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from R2: " + e.getMessage(), e);
        }
    }

    /**
     * Get a file from R2/S3
     * 
     * @param objectKey The object key to retrieve
     * @return The file input stream
     */
    public InputStream getFile(String objectKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get file from R2: " + e.getMessage(), e);
        }
    }

    /**
     * Check if a file exists in R2/S3
     * 
     * @param objectKey The object key to check
     * @return true if file exists, false otherwise
     */
    public boolean fileExists(String objectKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Failed to check if file exists in R2: " + e.getMessage(), e);
        }
    }

    /**
     * Generate a unique object key for the file
     * 
     * @param userId   The user ID
     * @param fileName The original file name
     * @return The generated object key
     */
    private String generateObjectKey(Long userId, String fileName) {
        String uuid = UUID.randomUUID().toString();
        return String.format("users/%d/notes/%s/%s", userId, uuid, fileName);
    }

    /**
     * Get file size from R2/S3
     * 
     * @param objectKey The object key
     * @return The file size in bytes
     */
    public Long getFileSize(String objectKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentLength();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get file size from R2: " + e.getMessage(), e);
        }
    }
}