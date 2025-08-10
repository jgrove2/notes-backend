package dev.jgrove2.notes_backend.Controllers;

import dev.jgrove2.notes_backend.Models.User;
import dev.jgrove2.notes_backend.Services.UserService;
import dev.jgrove2.notes_backend.Utils.TokenExtractionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenExtractionUtil tokenExtractionUtil;

    /**
     * Get current user profile from JWT token
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            // Extract kinde_user_id from JWT token
            String kindeUserId = tokenExtractionUtil.extractKindeUserIdFromHeader(authorizationHeader);
            if (kindeUserId == null || kindeUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: missing subject claim"));
            }

            // Find user in database by kinde_user_id
            Optional<User> userOptional = userService.getUserByKindeUserId(kindeUserId);

            if (userOptional.isPresent()) {
                User user = userOptional.get();
                return ResponseEntity.ok(user);
            } else {
                // User not found in database
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of(
                                "error", "User profile not found",
                                "kinde_user_id", kindeUserId,
                                "message", "User profile does not exist in the database"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Authentication failed: " + e.getMessage()));
        }
    }

    /**
     * Create user profile (for first-time users)
     */
    @PostMapping("/profile")
    public ResponseEntity<?> createUserProfile(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> request) {

        try {
            // Extract kinde_user_id from JWT token
            String kindeUserId = tokenExtractionUtil.extractKindeUserIdFromHeader(authorizationHeader);
            if (kindeUserId == null || kindeUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: missing subject claim"));
            }

            // Get user data from request
            String firstName = request.get("firstName");
            String lastName = request.get("lastName");

            if (firstName == null || lastName == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "firstName and lastName are required"));
            }

            // Check if user already exists
            if (userService.userExistsByKindeUserId(kindeUserId)) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "User profile already exists"));
            }

            // Create new user profile
            User user = userService.createUser(firstName, lastName, kindeUserId);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create user profile: " + e.getMessage()));
        }
    }

    /**
     * Update user profile (partial fields allowed)
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateUserProfile(
            @RequestHeader("Authorization") String authorizationHeader,
            @RequestBody Map<String, String> request) {

        try {
            // Extract kinde_user_id from JWT token
            String kindeUserId = tokenExtractionUtil.extractKindeUserIdFromHeader(authorizationHeader);
            if (kindeUserId == null || kindeUserId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Invalid token: missing subject claim"));
            }

            // Get user data from request (optional fields)
            String firstName = request.getOrDefault("firstName", null);
            String lastName = request.getOrDefault("lastName", null);

            if (firstName == null && lastName == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "At least one of firstName or lastName must be provided"));
            }

            // Find user in database
            Optional<User> userOptional = userService.getUserByKindeUserId(kindeUserId);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "User profile not found"));
            }

            // Update only provided fields (names only)
            User updatedUser = userService.updateUserNames(userOptional.get().getUserId(), firstName, lastName);

            return ResponseEntity.ok(updatedUser);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update user profile: " + e.getMessage()));
        }
    }
}