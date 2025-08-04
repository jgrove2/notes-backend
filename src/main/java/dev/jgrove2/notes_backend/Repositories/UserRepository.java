package dev.jgrove2.notes_backend.Repositories;

import dev.jgrove2.notes_backend.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by Kinde user ID
     */
    Optional<User> findByKindeUserId(String kindeUserId);

    /**
     * Find active user by Kinde user ID
     */
    Optional<User> findByKindeUserIdAndIsActiveTrue(String kindeUserId);

    /**
     * Check if user exists by Kinde user ID
     */
    boolean existsByKindeUserId(String kindeUserId);
}