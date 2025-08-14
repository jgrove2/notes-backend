package dev.jgrove2.notes_backend.Utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class UploadThingKeyUtil {

    private static int djb2(String s) {
        int h = 5381;
        for (int i = s.length() - 1; i >= 0; i--) {
            h = (h * 33) ^ s.charAt(i);
        }
        // mimic js bitwise ops behavior
        int masked = (h & 0xBFFFFFFF) | ((h >>> 1) & 0x40000000);
        return masked;
    }

    private static String shuffle(String str, String seed) {
        char[] chars = str.toCharArray();
        int seedNum = djb2(seed);
        for (int i = 0; i < chars.length; i++) {
            int j = ((seedNum % (i + 1)) + i) % chars.length;
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    // A minimal alphabet to simulate sqids; you can substitute with a full impl
    private static final String DEFAULT_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private static String encodeAppId(String appId) {
        String alphabet = shuffle(DEFAULT_ALPHABET, appId);
        // Not a true sqids encoding; we just prefix a 12-char deterministic token
        String hash = Integer.toUnsignedString(Math.abs(djb2(appId)), 36);
        if (hash.length() < 12) {
            hash = (hash + "000000000000").substring(0, 12);
        } else if (hash.length() > 12) {
            hash = hash.substring(0, 12);
        }
        // map onto shuffled alphabet deterministically
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            int idx = Math.abs(hash.charAt(i)) % alphabet.length();
            sb.append(alphabet.charAt(idx));
        }
        return sb.toString();
    }

    private static String encodeSeedUrlSafe(String seed) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(seed.getBytes(StandardCharsets.UTF_8));
    }

    public static String generateFileKey(String appId, String fileSeed) {
        String encodedAppId = encodeAppId(appId);
        String encodedSeed = encodeSeedUrlSafe(fileSeed);
        return encodedAppId + encodedSeed;
    }

    public static String hmacSha256Hex(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }
}