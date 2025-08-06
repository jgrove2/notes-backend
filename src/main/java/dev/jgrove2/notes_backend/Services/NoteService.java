package dev.jgrove2.notes_backend.Services;

import dev.jgrove2.notes_backend.Models.Note;
import dev.jgrove2.notes_backend.Repositories.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    /**
     * Create a new note
     */
    public Note createNote(String fileName, Long userId, Long totalSizeBytes, String objectKey) {
        // Check if note already exists for this user and file name
        if (noteRepository.existsByUserIdAndFileName(userId, fileName)) {
            throw new RuntimeException("Note with file name '" + fileName + "' already exists for this user");
        }

        Note note = new Note(fileName, userId, totalSizeBytes, objectKey);
        return noteRepository.save(note);
    }

    /**
     * Get all notes for a user
     */
    public List<Note> getNotesByUserId(Long userId) {
        return noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    /**
     * Get note by user ID and file name
     */
    public Optional<Note> getNoteByUserIdAndFileName(Long userId, String fileName) {
        return noteRepository.findByUserIdAndFileName(userId, fileName);
    }

    /**
     * Update note file size
     */
    public Note updateNoteSize(Long userId, String fileName, Long newTotalSizeBytes) {
        Optional<Note> noteOptional = noteRepository.findByUserIdAndFileName(userId, fileName);

        if (noteOptional.isPresent()) {
            Note note = noteOptional.get();
            note.setTotalSizeBytes(newTotalSizeBytes);
            note.setLastModifiedDate(java.time.LocalDateTime.now());
            return noteRepository.save(note);
        } else {
            throw new RuntimeException("Note not found: " + fileName);
        }
    }

    /**
     * Update note with new file
     */
    public Note updateNote(Long userId, String fileName, Long newTotalSizeBytes) {
        return updateNoteSize(userId, fileName, newTotalSizeBytes);
    }

    /**
     * Delete note
     */
    public void deleteNote(Long userId, String fileName) {
        Optional<Note> noteOptional = noteRepository.findByUserIdAndFileName(userId, fileName);

        if (noteOptional.isPresent()) {
            noteRepository.delete(noteOptional.get());
        } else {
            throw new RuntimeException("Note not found: " + fileName);
        }
    }

    /**
     * Get note count for user
     */
    public long getNoteCountByUserId(Long userId) {
        return noteRepository.countByUserId(userId);
    }

    /**
     * Check if note exists
     */
    public boolean noteExists(Long userId, String fileName) {
        return noteRepository.existsByUserIdAndFileName(userId, fileName);
    }

    /**
     * Get total storage size for a user
     */
    public Long getTotalStorageSizeByUserId(Long userId) {
        List<Note> notes = noteRepository.findByUserId(userId);
        return notes.stream()
                .mapToLong(Note::getTotalSizeBytes)
                .sum();
    }

    /**
     * Build file structure for a user
     * Creates a hierarchical structure from filenames that contain full paths
     */
    public Map<String, Object> buildFileStructure(Long userId) {
        List<Note> notes = noteRepository.findByUserIdOrderByCreatedAtDesc(userId);
        Map<String, Object> fileStructure = new HashMap<>();

        for (Note note : notes) {
            String fileName = note.getFileName();
            String[] pathParts = fileName.split("/");

            Map<String, Object> currentLevel = fileStructure;

            // Build the path structure
            for (int i = 0; i < pathParts.length - 1; i++) {
                String folderName = pathParts[i];

                if (!currentLevel.containsKey(folderName)) {
                    currentLevel.put(folderName, new HashMap<String, Object>());
                }

                @SuppressWarnings("unchecked")
                Map<String, Object> nextLevel = (Map<String, Object>) currentLevel.get(folderName);
                currentLevel = nextLevel;
            }

            // Add the file at the final level (just the filename, no metadata)
            String fileNameOnly = pathParts[pathParts.length - 1];
            currentLevel.put(fileNameOnly, null);
        }

        return fileStructure;
    }
}