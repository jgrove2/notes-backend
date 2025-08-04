package dev.jgrove2.notes_backend.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtParser;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.security.KeyFactory;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.jgrove2.notes_backend.Config.AppConfig;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class JwtTokenUtil {

    private static final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();

    @Autowired
    private AppConfig appConfig;

    @jakarta.annotation.PostConstruct
    public void init() {
        // Don't preload keys during startup to avoid blocking application startup
        // Keys will be loaded on-demand when tokens are validated
    }

    public Claims parseToken(String token) {
        try {
            // Get allowed issuers from configuration
            String[] allowedIssuers = appConfig.getAllowedIssuers().split(",");

            String kid = getKidFromTokenHeader(token);
            PublicKey publicKey = getPublicKeyFromJwks(kid);

            JwtParser parser = Jwts.parser().verifyWith(publicKey).build();

            Jws<Claims> jwsClaims = parser.parseSignedClaims(token);
            Claims claims = jwsClaims.getPayload();

            // Check issuer if present
            String issuer = claims.getIssuer();
            if (issuer != null && !issuer.isEmpty()) {
                boolean issuerAllowed = false;
                for (String allowed : allowedIssuers) {
                    if (allowed.trim().equals(issuer.trim())) {
                        issuerAllowed = true;
                        break;
                    }
                }
                if (!issuerAllowed) {
                    throw new RuntimeException("JWT issuer not allowed: " + issuer);
                }
            }

            return claims;
        } catch (Exception e) {
            throw new RuntimeException("JWT verification failed: " + e.getMessage(), e);
        }
    }

    private String getKidFromTokenHeader(String token) throws IOException {
        String[] parts = token.split("\\.");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }
        String headerJson = new String(Base64.getUrlDecoder().decode(parts[0]));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode header = mapper.readTree(headerJson);
        if (!header.has("kid")) {
            throw new IllegalArgumentException("JWT header missing 'kid'");
        }
        return header.get("kid").asText();
    }

    private PublicKey getPublicKeyFromJwks(String kid) throws Exception {
        if (keyCache.containsKey(kid)) {
            return keyCache.get(kid);
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jwks = mapper.readTree(new URL(appConfig.getJwksUrl()));
            JsonNode keys = jwks.get("keys");
            if (keys == null || !keys.isArray()) {
                throw new RuntimeException("Invalid JWKS format");
            }

            for (JsonNode key : keys) {
                if (key.has("kid") && kid.equals(key.get("kid").asText())) {
                    String kty = key.get("kty").asText();
                    if (!"RSA".equals(kty)) {
                        throw new RuntimeException("Only RSA keys are supported");
                    }
                    String n = key.get("n").asText();
                    String e = key.get("e").asText();

                    byte[] modulusBytes = Base64.getUrlDecoder().decode(n);
                    byte[] exponentBytes = Base64.getUrlDecoder().decode(e);

                    BigInteger modulus = new BigInteger(1, modulusBytes);
                    BigInteger exponent = new BigInteger(1, exponentBytes);

                    RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);
                    KeyFactory kf = KeyFactory.getInstance("RSA");
                    PublicKey publicKey = kf.generatePublic(spec);

                    keyCache.put(kid, publicKey);
                    return publicKey;
                }
            }
            throw new RuntimeException("Public key not found for kid: " + kid);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch public key from JWKS: " + e.getMessage(), e);
        }
    }
}