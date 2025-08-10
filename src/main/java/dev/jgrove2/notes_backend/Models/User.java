package dev.jgrove2.notes_backend.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    private static final long DEFAULT_MAX_STORAGE_BYTES = 500000000L; // 0.5 GiB

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userid")
    private Long userId;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "kinde_user_id", unique = true, nullable = false)
    private String kindeUserId;

    @Column(name = "max_storage")
    private Long maxStorage;

    @Column(name = "auto_save")
    private Boolean autoSave;

    @Column(name = "auto_save_duration")
    private Integer autoSaveDuration;

    // Default constructor
    public User() {
    }

    // Constructor with required fields
    public User(String firstName, String lastName, String kindeUserId) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.kindeUserId = kindeUserId;
        this.isActive = true;
        this.lastModifiedDate = LocalDateTime.now();
        this.maxStorage = DEFAULT_MAX_STORAGE_BYTES;
        this.autoSave = Boolean.FALSE;
        this.autoSaveDuration = null;
    }

    // Getters and Setters
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public LocalDateTime getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDateTime lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
        this.lastModifiedDate = LocalDateTime.now();
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

    public String getKindeUserId() {
        return kindeUserId;
    }

    public void setKindeUserId(String kindeUserId) {
        this.kindeUserId = kindeUserId;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public Long getMaxStorage() {
        return maxStorage;
    }

    public void setMaxStorage(Long maxStorage) {
        this.maxStorage = maxStorage;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public Boolean getAutoSave() {
        return autoSave;
    }

    public void setAutoSave(Boolean autoSave) {
        this.autoSave = autoSave;
        this.lastModifiedDate = LocalDateTime.now();
    }

    public Integer getAutoSaveDuration() {
        return autoSaveDuration;
    }

    public void setAutoSaveDuration(Integer autoSaveDuration) {
        this.autoSaveDuration = autoSaveDuration;
        this.lastModifiedDate = LocalDateTime.now();
    }

    // toString method for debugging
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", lastModifiedDate=" + lastModifiedDate +
                ", isActive=" + isActive +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", kindeUserId='" + kindeUserId + '\'' +
                ", maxStorage=" + maxStorage +
                ", autoSave=" + autoSave +
                ", autoSaveDuration=" + autoSaveDuration +
                '}';
    }

    // equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        User user = (User) o;
        return userId != null && userId.equals(user.userId);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}