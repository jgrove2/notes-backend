package dev.jgrove2.notes_backend.Repositories;

import dev.jgrove2.notes_backend.Models.Note;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    /**
     * Find all notes by user ID
     */
    List<Note> findByUserId(Long userId);

    /**
     * Find note by user ID and file name
     */
    Optional<Note> findByUserIdAndFileName(Long userId, String fileName);

    /**
     * Check if note exists by user ID and file name
     */
    boolean existsByUserIdAndFileName(Long userId, String fileName);

    /**
     * Find all notes by user ID ordered by creation date (newest first)
     */
    List<Note> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Count notes by user ID
     */
    long countByUserId(Long userId);
}