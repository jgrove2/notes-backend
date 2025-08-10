package dev.jgrove2.notes_backend.Controllers;

import dev.jgrove2.notes_backend.Models.Note;
import dev.jgrove2.notes_backend.Models.User;
import dev.jgrove2.notes_backend.Services.NoteService;
import dev.jgrove2.notes_backend.Services.S3Service;
import dev.jgrove2.notes_backend.Services.UserService;
import dev.jgrove2.notes_backend.Utils.TokenExtractionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    @Autowired
    private TokenExtractionUtil tokenExtractionUtil;

    @Autowired
    private S3Service s3Service;

    /**
     * Create a new note with file upload
     */
    @PostMapping
    public ResponseEntity<?> createNote(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename) {

        try {
            // Extract kinde_user_id from JWT token
            String kindeUserId = tokenExtractionUtil.extractKindeUserIdFromHeader(authorizationHeader);
            if (kindeUserId == null || kindeUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: missing subject claim"));
            }

            // Get user from database
            Optional<User> userOptional = userService.getUserByKindeUserId(kindeUserId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User profile not found"));
            }

            User user = userOptional.get();
            Long userId = user.getUserId();
            Long fileSize = file.getSize();

            // Enforce storage quota before upload
            Long maxStorage = user.getMaxStorage();
            Long currentTotal = noteService.getTotalStorageSizeByUserId(userId);
            long wouldBeTotal = (currentTotal == null ? 0L : currentTotal) + (fileSize == null ? 0L : fileSize);
            if (maxStorage != null && wouldBeTotal > maxStorage) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(Map.of("error", "Storage limit exceeded"));
            }

            // Upload file to R2
            String objectKey = s3Service.uploadFile(file.getInputStream(), filename, userId);

            // Create note
            Note note = noteService.createNote(filename, userId, fileSize, objectKey);

            return ResponseEntity.status(HttpStatus.CREATED).body(note);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create note: " + e.getMessage()));
        }
    }

    /**
     * Get all notes for the current user
     */
    @GetMapping
    public ResponseEntity<?> getNotes(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract kinde_user_id from JWT token
            String kindeUserId = tokenExtractionUtil.extractKindeUserIdFromHeader(authorizationHeader);
            if (kindeUserId == null || kindeUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: missing subject claim"));
            }

            // Get user from database
            Optional<User> userOptional = userService.getUserByKindeUserId(kindeUserId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User profile not found"));
            }

            User user = userOptional.get();
            Long userId = user.getUserId();

            // Get all notes for the user
            List<Note> notes = noteService.getNotesByUserId(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("notes", notes);
            response.put("count", notes.size());
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get notes: " + e.getMessage()));
        }
    }

    /**
     * Update note with new file
     */
    @PutMapping
    public ResponseEntity<?> updateNote(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam("filename") String filename) {

        try {
            // Extract kinde_user_id from JWT token
            String kindeUserId = tokenExtractionUtil.extractKindeUserIdFromHeader(authorizationHeader);
            if (kindeUserId == null || kindeUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: missing subject claim"));
            }

            // Get user from database
            Optional<User> userOptional = userService.getUserByKindeUserId(kindeUserId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User profile not found"));
            }

            User user = userOptional.get();
            Long userId = user.getUserId();
            Long newFileSize = file.getSize();

            // Get existing note to find the object key and current size
            Optional<Note> existingNote = noteService.getNoteByUserIdAndFileName(userId, filename);
            if (!existingNote.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Note not found: " + filename));
            }

            String objectKey = existingNote.get().getObjectKey();
            long existingSize = existingNote.get().getTotalSizeBytes() == null ? 0L
                    : existingNote.get().getTotalSizeBytes();

            // Enforce storage quota before upload (account for replacement)
            Long maxStorage = user.getMaxStorage();
            Long currentTotal = noteService.getTotalStorageSizeByUserId(userId);
            long wouldBeTotal = (currentTotal == null ? 0L : currentTotal) - existingSize
                    + (newFileSize == null ? 0L : newFileSize);
            if (maxStorage != null && wouldBeTotal > maxStorage) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                        .body(Map.of("error", "Storage limit exceeded"));
            }

            // Update file in R2
            s3Service.updateFile(file.getInputStream(), objectKey);

            // Update note in database
            Note updatedNote = noteService.updateNote(userId, filename, newFileSize);

            return ResponseEntity.ok(updatedNote);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update note: " + e.getMessage()));
        }
    }

    /**
     * Get note info by filename
     */
    @GetMapping("/info")
    public ResponseEntity<?> getNoteInfoByFilename(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("filename") String filename) {

        try {
            // Extract kinde_user_id from JWT token
            String kindeUserId = tokenExtractionUtil.extractKindeUserIdFromHeader(authorizationHeader);
            if (kindeUserId == null || kindeUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: missing subject claim"));
            }

            // Get user from database
            Optional<User> userOptional = userService.getUserByKindeUserId(kindeUserId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User profile not found"));
            }

            User user = userOptional.get();
            Long userId = user.getUserId();

            // Get note by filename
            Optional<Note> noteOptional = noteService.getNoteByUserIdAndFileName(userId, filename);

            if (noteOptional.isPresent()) {
                return ResponseEntity.ok(noteOptional.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Note not found: " + filename));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get note info: " + e.getMessage()));
        }
    }

    /**
     * Get note content by filename (returns the actual HTML file from S3)
     */
    @GetMapping("/content")
    public ResponseEntity<?> getNoteContentByFilename(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("filename") String filename) {

        try {
            // Extract kinde_user_id from JWT token
            String kindeUserId = tokenExtractionUtil.extractKindeUserIdFromHeader(authorizationHeader);
            if (kindeUserId == null || kindeUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: missing subject claim"));
            }

            // Get user from database
            Optional<User> userOptional = userService.getUserByKindeUserId(kindeUserId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User profile not found"));
            }

            User user = userOptional.get();
            Long userId = user.getUserId();

            // Get note by filename to retrieve the object key
            Optional<Note> noteOptional = noteService.getNoteByUserIdAndFileName(userId, filename);
            if (!noteOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Note not found: " + filename));
            }

            Note note = noteOptional.get();
            String objectKey = note.getObjectKey();

            // Check if file exists in S3
            if (!s3Service.fileExists(objectKey)) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Note file not found in storage: " + filename));
            }

            // Get file content from S3
            InputStream fileContent = s3Service.getFile(objectKey);

            // Convert InputStream to byte array for response
            byte[] content = fileContent.readAllBytes();
            fileContent.close();

            return ResponseEntity.ok()
                    .header("Content-Type", "text/html; charset=UTF-8")
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .body(content);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get note content: " + e.getMessage()));
        }
    }

    /**
     * Delete note by filename
     */
    @DeleteMapping
    public ResponseEntity<?> deleteNote(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("filename") String filename) {

        try {
            // Extract kinde_user_id from JWT token
            String kindeUserId = tokenExtractionUtil.extractKindeUserIdFromHeader(authorizationHeader);
            if (kindeUserId == null || kindeUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: missing subject claim"));
            }

            // Get user from database
            Optional<User> userOptional = userService.getUserByKindeUserId(kindeUserId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User profile not found"));
            }

            User user = userOptional.get();
            Long userId = user.getUserId();

            // Get existing note to find the object key
            Optional<Note> existingNote = noteService.getNoteByUserIdAndFileName(userId, filename);
            if (!existingNote.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Note not found: " + filename));
            }

            String objectKey = existingNote.get().getObjectKey();

            // Delete file from R2
            s3Service.deleteFile(objectKey);

            // Delete note from database
            noteService.deleteNote(userId, filename);

            return ResponseEntity.ok(Map.of("message", "Note deleted successfully: " + filename));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete note: " + e.getMessage()));
        }
    }

    /**
     * Rename a note's filename (object key remains unchanged)
     */
    @PostMapping("/rename")
    public ResponseEntity<?> renameNote(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestParam("oldFilename") String oldFilename,
            @RequestParam("newFilename") String newFilename) {
        try {
            // Extract kinde_user_id from JWT token
            String kindeUserId = tokenExtractionUtil.extractKindeUserIdFromHeader(authorizationHeader);
            if (kindeUserId == null || kindeUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: missing subject claim"));
            }

            // Get user from database
            Optional<User> userOptional = userService.getUserByKindeUserId(kindeUserId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User profile not found"));
            }

            User user = userOptional.get();
            Long userId = user.getUserId();

            // Rename the note (does not touch the object key)
            Note updated = noteService.renameNote(userId, oldFilename, newFilename);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", ex.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to rename note: " + e.getMessage()));
        }
    }

    /**
     * Get total storage size for the current user
     */
    @GetMapping("/storage/size")
    public ResponseEntity<?> getUserStorageSize(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract kinde_user_id from JWT token
            String kindeUserId = tokenExtractionUtil.extractKindeUserIdFromHeader(authorizationHeader);
            if (kindeUserId == null || kindeUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: missing subject claim"));
            }

            // Get user from database
            Optional<User> userOptional = userService.getUserByKindeUserId(kindeUserId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User profile not found"));
            }

            User user = userOptional.get();
            Long userId = user.getUserId();

            // Calculate total storage size
            Long totalSizeBytes = noteService.getTotalStorageSizeByUserId(userId);
            long noteCount = noteService.getNoteCountByUserId(userId);

            // Convert to different units for better readability
            Map<String, Object> sizeInfo = calculateSizeInfo(totalSizeBytes);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("totalSizeBytes", totalSizeBytes);
            response.put("noteCount", noteCount);
            response.put("sizeInfo", sizeInfo);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get storage size: " + e.getMessage()));
        }
    }

    /**
     * Get file structure for the current user
     * Returns a hierarchical structure representing the user's file organization
     */
    @GetMapping("/structure")
    public ResponseEntity<?> getFileStructure(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract kinde_user_id from JWT token
            String kindeUserId = tokenExtractionUtil.extractKindeUserIdFromHeader(authorizationHeader);
            if (kindeUserId == null || kindeUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: missing subject claim"));
            }

            // Get user from database
            Optional<User> userOptional = userService.getUserByKindeUserId(kindeUserId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User profile not found"));
            }

            User user = userOptional.get();
            Long userId = user.getUserId();

            // Build file structure
            Map<String, Object> fileStructure = noteService.buildFileStructure(userId);
            long noteCount = noteService.getNoteCountByUserId(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("userId", userId);
            response.put("noteCount", noteCount);
            response.put("fileStructure", fileStructure);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get file structure: " + e.getMessage()));
        }
    }

    /**
     * Calculate size information in different units
     */
    private Map<String, Object> calculateSizeInfo(Long totalSizeBytes) {
        Map<String, Object> sizeInfo = new HashMap<>();

        // Convert to different units
        double sizeInKB = totalSizeBytes / 1024.0;
        double sizeInMB = sizeInKB / 1024.0;
        double sizeInGB = sizeInMB / 1024.0;

        sizeInfo.put("bytes", totalSizeBytes);
        sizeInfo.put("kilobytes", Math.round(sizeInKB * 100.0) / 100.0);
        sizeInfo.put("megabytes", Math.round(sizeInMB * 100.0) / 100.0);
        sizeInfo.put("gigabytes", Math.round(sizeInGB * 100.0) / 100.0);

        return sizeInfo;
    }
}