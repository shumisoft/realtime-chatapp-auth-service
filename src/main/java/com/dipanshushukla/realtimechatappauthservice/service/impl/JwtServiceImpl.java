package com.dipanshushukla.realtimechatappauthservice.service.impl;

import java.io.ByteArrayInputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.converter.RsaKeyConverters;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.dipanshushukla.realtimechatappauthservice.service.JwtService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Data
public class JwtServiceImpl implements JwtService {

    @Value("${jwt.public-key}")
    private String publicKeyPem;

    @Value("${jwt.private-key}")
    private String privateKeyPem;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    @Override
    @PostConstruct
    public void init() {
        try {
            log.info("Loading RSA keys from config...");

            this.privateKey = RsaKeyConverters.pkcs8()
                    .convert(new ByteArrayInputStream(privateKeyPem.getBytes()));

            this.publicKey = RsaKeyConverters.x509()
                    .convert(new ByteArrayInputStream(publicKeyPem.getBytes()));

            log.info("Keys loaded successfully");
        } catch (Exception e) {
            log.error("Failed to load RSA keys", e);
            throw new RuntimeException("Failed to initialize JwtService", e);
        }
    }

    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public boolean isValid(String token, UserDetails user) {
        String username = extractUsername(token);
        return username.equals(user.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    @Override
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    @Override
    public String generateAccessToken(UUID userId, String username) {
        return Jwts
                .builder()
                .setSubject(username)
                .claim("userId", userId.toString())
                .claim("username", username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 6L * 60 * 60 * 1000))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    @Override
    public String generateRefreshToken(UUID userId, String username) {
        return Jwts
                .builder()
                .setSubject(username)
                .claim("userId", userId.toString())
                .claim("username", username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    @Override
    public Object getJwks() {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) this.getPublicKey();

        Map<String, Object> jwk = new HashMap<>();
        jwk.put("kty", "RSA");
        jwk.put("kid", "auth-service-key"); // stable key ID
        jwk.put("n", base64Url(rsaPublicKey.getModulus().toByteArray()));
        jwk.put("e", base64Url(rsaPublicKey.getPublicExponent().toByteArray()));

        return Map.of("keys", List.of(jwk));

    }

    private String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);
    }
}
