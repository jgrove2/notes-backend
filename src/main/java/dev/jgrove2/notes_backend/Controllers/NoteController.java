package dev.jgrove2.notes_backend.Controllers;

import dev.jgrove2.notes_backend.Models.Note;
import dev.jgrove2.notes_backend.Models.User;
import dev.jgrove2.notes_backend.Services.NoteService;
import dev.jgrove2.notes_backend.Services.UserService;
import dev.jgrove2.notes_backend.Utils.TokenExtractionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
            String objectKey = "testing123123123";

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
    @PutMapping("/{filename}")
    public ResponseEntity<?> updateNote(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String filename,
            @RequestParam("file") MultipartFile file) {

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

            // Update note
            Note updatedNote = noteService.updateNote(userId, filename, newFileSize);

            return ResponseEntity.ok(updatedNote);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update note: " + e.getMessage()));
        }
    }

    /**
     * Get note by filename
     */
    @GetMapping("/{filename}")
    public ResponseEntity<?> getNoteByFilename(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String filename) {

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
                    .body(Map.of("error", "Failed to get note: " + e.getMessage()));
        }
    }

    /**
     * Delete note by filename
     */
    @DeleteMapping("/{filename}")
    public ResponseEntity<?> deleteNote(
            @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String filename) {

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

            // Delete note
            noteService.deleteNote(userId, filename);

            return ResponseEntity.ok(Map.of("message", "Note deleted successfully: " + filename));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete note: " + e.getMessage()));
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