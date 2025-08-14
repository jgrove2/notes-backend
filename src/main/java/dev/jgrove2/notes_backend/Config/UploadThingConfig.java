package dev.jgrove2.notes_backend.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UploadThingConfig {

    @Value("${uploadthing.api-key:}")
    private String apiKey;

    @Value("${uploadthing.app-id:}")
    private String appId;

    @Value("${uploadthing.region:us-east-1}")
    private String regionAlias;

    @Value("${uploadthing.callback-url:}")
    private String callbackUrl;

    @Value("${uploadthing.url-expiry-seconds:3600}")
    private long urlExpirySeconds;

    public String getApiKey() {
        return apiKey;
    }

    public String getAppId() {
        return appId;
    }

    public String getRegionAlias() {
        return regionAlias;
    }

    public String getCallbackUrl() {
        return callbackUrl;
    }

    public long getUrlExpirySeconds() {
        return urlExpirySeconds;
    }
}