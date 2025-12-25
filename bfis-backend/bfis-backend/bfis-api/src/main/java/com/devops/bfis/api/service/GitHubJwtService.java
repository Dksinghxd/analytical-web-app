package com.devops.bfis.api.service;

import com.devops.bfis.api.config.GitHubAppConfig;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.stereotype.Service;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.security.KeyFactory;
import java.security.Security;

/**
 * Service for generating GitHub App JWT tokens
 * 
 * GitHub Apps use JWT for authentication when making API calls
 * The JWT is signed with the app's private key using RS256 algorithm
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GitHubJwtService {
    private final GitHubAppConfig config;
    
    /**
     * Generate a JWT token for GitHub App authentication
     * 
     * Token format:
     * - Header: { "alg": "RS256", "typ": "JWT" }
     * - Payload: { "iat": issued_at, "exp": expires_at, "iss": app_id }
     * - Signature: RS256 with private key
     * 
     * Valid for 10 minutes (GitHub's maximum)
     * 
     * @return JWT token string
     */
    public String generateJwt() {
        try {
            Instant now = Instant.now();
            Instant expiration = now.plusSeconds(600); // 10 minutes
            
            String configuredKey = resolvePrivateKeyValue();
            PrivateKey privateKey = parsePrivateKey(configuredKey);
            
            String jwt = Jwts.builder()
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(expiration))
                    .setIssuer(config.getAppId())
                    .signWith(privateKey, SignatureAlgorithm.RS256)
                    .compact();
            
            log.debug("Generated GitHub App JWT (expires in 10 minutes)");
            return jwt;
            
        } catch (Exception e) {
            log.error("Failed to generate GitHub App JWT", e);
            throw new RuntimeException("Failed to generate GitHub App JWT", e);
        }
    }

    private String resolvePrivateKeyValue() {
        String path = config.getPrivateKeyPath();
        if (path != null && !path.isBlank()) {
            try {
                Path keyPath = Path.of(path.trim());
                if (!Files.exists(keyPath)) {
                    throw new IllegalArgumentException("GitHub private key path does not exist: " + keyPath);
                }
                if (!Files.isRegularFile(keyPath)) {
                    throw new IllegalArgumentException("GitHub private key path is not a file: " + keyPath);
                }
                return Files.readString(keyPath, StandardCharsets.UTF_8);
            } catch (Exception e) {
                throw new RuntimeException("Failed to read GitHub private key from path: " + path, e);
            }
        }
        return config.getPrivateKey();
    }
    
    /**
     * Parse base64-encoded private key from configuration
     * 
     * Accepted formats:
     * - Base64-encoded PKCS8 DER
     * - PEM text (PKCS8: BEGIN PRIVATE KEY, or PKCS1: BEGIN RSA PRIVATE KEY)
     * - Base64-encoded PEM text
     */
    private PrivateKey parsePrivateKey(String base64PrivateKey) throws Exception {
        if (base64PrivateKey == null || base64PrivateKey.isBlank()) {
            throw new IllegalArgumentException("GitHub private key is not configured");
        }

        String value = base64PrivateKey.trim();

        // Ensure BC provider is registered (safe to call multiple times)
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        // 1) If the value is PEM text directly, parse it.
        if (value.contains("BEGIN ")) {
            return parsePemToPrivateKey(value);
        }

        // 2) Otherwise, treat it as base64. It might decode to DER bytes or to PEM text.
        String cleanBase64 = value.replaceAll("\\s", "");
        byte[] decoded = Base64.getDecoder().decode(cleanBase64);

        // If decoded content looks like PEM, parse it as text.
        String decodedAsText = new String(decoded, StandardCharsets.US_ASCII);
        if (decodedAsText.contains("BEGIN ")) {
            return parsePemToPrivateKey(decodedAsText);
        }

        // Otherwise assume DER (PKCS8)
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(decoded));
    }

    private PrivateKey parsePemToPrivateKey(String pemText) throws Exception {
        try (PEMParser parser = new PEMParser(new StringReader(pemText))) {
            Object object = parser.readObject();
            if (object == null) {
                throw new IllegalArgumentException("Empty PEM content");
            }

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

            if (object instanceof PEMKeyPair keyPair) {
                return converter.getPrivateKey(keyPair.getPrivateKeyInfo());
            }
            if (object instanceof PrivateKeyInfo privateKeyInfo) {
                return converter.getPrivateKey(privateKeyInfo);
            }

            throw new IllegalArgumentException("Unsupported PEM object: " + object.getClass().getName());
        }
    }
}
