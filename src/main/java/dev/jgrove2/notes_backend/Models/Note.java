package dev.jgrove2.notes_backend.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "object_key", length = 500)
    private String objectKey;

    @Column(name = "total_size_bytes", nullable = false)
    private Long totalSizeBytes;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Default constructor
    public Note() {
    }

    // Constructor with required fields
    public Note(String fileName, Long userId, Long totalSizeBytes, String objectKey) {
        this.fileName = fileName;
        this.userId = userId;
        this.objectKey = objectKey;
        this.totalSizeBytes = totalSizeBytes;
    }

    // Constructor with all fields
    public Note(String fileName, Long userId, String objectKey, Long totalSizeBytes) {
        this.fileName = fileName;
        this.userId = userId;
        this.objectKey = objectKey;
        this.totalSizeBytes = totalSizeBytes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public Long getTotalSizeBytes() {
        return totalSizeBytes;
    }

    public void setTotalSizeBytes(Long totalSizeBytes) {
        this.totalSizeBytes = totalSizeBytes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", fileName='" + fileName + '\'' +
                ", userId=" + userId +
                ", objectKey='" + objectKey + '\'' +
                ", totalSizeBytes=" + totalSizeBytes +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }

    // equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Note note = (Note) o;
        return id != null && id.equals(note.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}