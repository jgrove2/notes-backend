package dev.jgrove2.notes_backend.Controllers;

import dev.jgrove2.notes_backend.Config.UploadThingConfig;
import dev.jgrove2.notes_backend.Utils.UploadThingKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/uploadthing")
public class UploadThingController {

    @Autowired
    private UploadThingConfig config;

    /**
     * GET /api/uploadthing
     * Returns minimal EndpointMetadata. Adjust as needed for your client.
     */
    @GetMapping
    public ResponseEntity<?> getEndpoints() {
        // Example: expose a single route slug "noteUploader" allowing html files up to
        // 10MB
        Map<String, Object> routes = new HashMap<>();
        Map<String, Object> noteUploader = new HashMap<>();
        noteUploader.put("maxFileCount", 1);
        noteUploader.put("fileTypes", List.of("text/html"));
        noteUploader.put("maxFileSize", 10 * 1024 * 1024);
        routes.put("noteUploader", noteUploader);
        return ResponseEntity.ok(routes);
    }

    /**
     * POST /api/uploadthing
     * Query: slug, actionType=upload
     * Body: expects { files: [{ name, size, type, customId? }], metadata? }
     * Returns: { presigned: [{ url, key }], callback: { url, slug } }
     */
    @PostMapping
    public ResponseEntity<?> prepareUpload(
            @RequestParam("slug") String slug,
            @RequestParam("actionType") String actionType,
            @RequestBody Map<String, Object> payload) {
        if (!"upload".equalsIgnoreCase(actionType)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Unsupported actionType"));
        }
        if (!StringUtils.hasText(config.getApiKey()) || !StringUtils.hasText(config.getAppId())) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "UploadThing not configured"));
        }

        List<Map<String, Object>> files = (List<Map<String, Object>>) payload.getOrDefault("files", List.of());
        List<Map<String, Object>> presigned = new ArrayList<>();

        long expiresMs = Instant.now().toEpochMilli() + config.getUrlExpirySeconds() * 1000;

        for (Map<String, Object> f : files) {
            String originalName = String.valueOf(f.getOrDefault("name", "file"));
            Number sizeNum = (Number) f.getOrDefault("size", 0);
            long size = sizeNum.longValue();
            String type = f.getOrDefault("type", "application/octet-stream").toString();
            String customId = f.containsKey("customId") ? String.valueOf(f.get("customId")) : null;

            String seed = UUID.randomUUID() + ":" + originalName + ":" + size;
            String fileKey = UploadThingKeyUtil.generateFileKey(config.getAppId(), seed);

            Map<String, String> params = new LinkedHashMap<>();
            params.put("expires", String.valueOf(expiresMs));
            params.put("x-ut-identifier", config.getAppId());
            params.put("x-ut-file-name", originalName);
            params.put("x-ut-file-size", String.valueOf(size));
            params.put("x-ut-slug", slug);
            params.put("x-ut-file-type", type);
            if (customId != null)
                params.put("x-ut-custom-id", customId);
            // optional content disposition and acl can be added here

            String baseUrl = "https://" + config.getRegionAlias() + ".ingest.uploadthing.com/" + fileKey;
            StringBuilder urlBuilder = new StringBuilder(baseUrl).append("?");
            boolean first = true;
            for (Map.Entry<String, String> e : params.entrySet()) {
                if (!first)
                    urlBuilder.append('&');
                urlBuilder.append(URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8))
                        .append('=')
                        .append(URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8));
                first = false;
            }
            String urlToSign = urlBuilder.toString();
            String signature = "hmac-sha256=" + UploadThingKeyUtil.hmacSha256Hex(urlToSign, config.getApiKey());
            String signedUrl = urlToSign + "&signature=" + URLEncoder.encode(signature, StandardCharsets.UTF_8);

            presigned.add(Map.of(
                    "url", signedUrl,
                    "key", fileKey));
        }

        Map<String, Object> callback = Map.of(
                "url", StringUtils.hasText(config.getCallbackUrl()) ? config.getCallbackUrl() : "",
                "slug", slug);

        Map<String, Object> resp = new HashMap<>();
        resp.put("presigned", presigned);
        resp.put("callback", callback);
        return ResponseEntity.ok(resp);
    }

    /**
     * Callback handler (webhook-like) to receive UploadThing completion events
     * The client should register this URL as callbackUrl when requesting presigned
     * URLs.
     */
    @PostMapping("/callback")
    public ResponseEntity<?> handleCallback(
            @RequestHeader(value = "x-uploadthing-signature", required = false) String signature,
            @RequestHeader(value = "uploadthing-hook", required = false) String hook,
            @RequestBody String body) {
        if (!StringUtils.hasText(signature) || !StringUtils.hasText(hook)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Missing signature or hook headers"));
        }
        // Verify signature HMAC-SHA256(body, apiKey)
        String expected = UploadThingKeyUtil.hmacSha256Hex(body, config.getApiKey());
        if (!signature.endsWith(expected)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid signature"));
        }
        // TODO: parse body JSON, persist metadata if desired, and optionally respond
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}