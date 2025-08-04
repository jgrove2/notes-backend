package dev.jgrove2.notes_backend.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TokenExtractionUtil {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Extract the kinde_user_id from the Authorization header
     * 
     * @param authorizationHeader The Authorization header value
     * @return The kinde_user_id if valid, null otherwise
     */
    public String extractKindeUserIdFromHeader(String authorizationHeader) {
        try {
            // Extract token from Authorization header
            String token = extractTokenFromHeader(authorizationHeader);
            if (token == null) {
                return null;
            }

            // Extract kinde_user_id from JWT token
            return jwtTokenUtil.getKindeUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Extract the JWT token from the Authorization header
     * 
     * @param authorizationHeader The Authorization header value
     * @return The JWT token if valid, null otherwise
     */
    public String extractTokenFromHeader(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        return null;
    }

    /**
     * Validate and extract kinde_user_id from Authorization header
     * 
     * @param authorizationHeader The Authorization header value
     * @return The kinde_user_id if valid, throws exception otherwise
     * @throws RuntimeException if token is invalid or missing
     */
    public String validateAndExtractKindeUserId(String authorizationHeader) {
        String kindeUserId = extractKindeUserIdFromHeader(authorizationHeader);

        if (kindeUserId == null || kindeUserId.isEmpty()) {
            throw new RuntimeException("Invalid or missing kinde_user_id in token");
        }

        return kindeUserId;
    }

    /**
     * Check if the Authorization header contains a valid Bearer token
     * 
     * @param authorizationHeader The Authorization header value
     * @return true if valid Bearer token, false otherwise
     */
    public boolean isValidBearerToken(String authorizationHeader) {
        return extractTokenFromHeader(authorizationHeader) != null;
    }
}