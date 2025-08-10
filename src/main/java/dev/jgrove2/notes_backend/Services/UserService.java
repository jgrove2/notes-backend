package dev.jgrove2.notes_backend.Services;

import dev.jgrove2.notes_backend.Models.User;
import dev.jgrove2.notes_backend.Repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Get user by Kinde user ID
     */
    public Optional<User> getUserByKindeUserId(String kindeUserId) {
        return userRepository.findByKindeUserIdAndIsActiveTrue(kindeUserId);
    }

    /**
     * Get user by ID
     */
    public Optional<User> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Create a new user
     */
    public User createUser(String firstName, String lastName, String kindeUserId) {
        if (userRepository.existsByKindeUserId(kindeUserId)) {
            throw new RuntimeException("User with Kinde ID " + kindeUserId + " already exists");
        }

        User user = new User(firstName, lastName, kindeUserId);
        return userRepository.save(user);
    }

    /**
     * Update user (both names required)
     */
    public User updateUser(Long userId, String firstName, String lastName) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            return userRepository.save(user);
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    /**
     * Partially update user fields (names only). Pass null for fields that should
     * not change.
     */
    public User updateUserNames(Long userId, String firstName, String lastName) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (firstName != null) {
                user.setFirstName(firstName);
            }
            if (lastName != null) {
                user.setLastName(lastName);
            }
            return userRepository.save(user);
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    /**
     * Check if user exists by Kinde user ID
     */
    public boolean userExistsByKindeUserId(String kindeUserId) {
        return userRepository.existsByKindeUserId(kindeUserId);
    }
}