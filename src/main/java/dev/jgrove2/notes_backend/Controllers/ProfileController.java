package dev.jgrove2.notes_backend.Controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    @GetMapping("/user")
    public Map<String, Object> getUserInfo() {
        // In a real application, you would extract user info from the JWT token
        return Map.of(
                "message", "User information endpoint",
                "status", "authenticated");
    }
}