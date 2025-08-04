package dev.jgrove2.notes_backend.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Value("${jwt.jwks-url:https://jgrove.kinde.com/.well-known/jwks}")
    private String jwksUrl;

    @Value("${jwt.allowed-issuers:https://jgrove.kinde.com}")
    private String allowedIssuers;

    @Value("${security.jwt.enabled:true}")
    private boolean jwtEnabled;

    @Value("${security.public-paths:/,/health,/public/**}")
    private String publicPaths;

    @Value("${spring.application.name:notes-backend}")
    private String applicationName;

    @Value("${server.port:8080}")
    private int serverPort;

    public String getJwksUrl() {
        return jwksUrl;
    }

    public String getAllowedIssuers() {
        return allowedIssuers;
    }

    public boolean isJwtEnabled() {
        return jwtEnabled;
    }

    public String getPublicPaths() {
        return publicPaths;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public int getServerPort() {
        return serverPort;
    }
}