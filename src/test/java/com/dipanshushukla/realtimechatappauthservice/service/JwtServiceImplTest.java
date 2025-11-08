package com.dipanshushukla.realtimechatappauthservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.dipanshushukla.realtimechatappauthservice.entity.User;
import com.dipanshushukla.realtimechatappauthservice.service.impl.JwtServiceImpl;

class JwtServiceImplTest {

  private JwtService jwtService;
  private UUID testUserId;
  private String testUsername;

  @BeforeEach
  void setUp() throws Exception {

    jwtService = new JwtServiceImpl();
    testUserId = UUID.randomUUID();
    testUsername = "test_user";

    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();

    jwtService.setPrivateKey(keyPair.getPrivate());
    jwtService.setPublicKey(keyPair.getPublic());

  }

  @Test
  @DisplayName("generateAccessToken: Should generate a valid access token containing correct claims")
  void generateAccessToken_ShouldReturnValidToken() {

    // Act
    String token = jwtService.generateAccessToken(testUserId, testUsername);

    // Assert
    assertNotNull(token, "Token should not be null");
    assertFalse(token.trim().isEmpty(), "Token should not be empty");

    assertEquals(testUsername, jwtService.extractUsername(token), "Extracted username should match");

    String extractedUserId = jwtService.extractClaim(token, claims -> claims.get("userId", String.class));
    assertEquals(testUserId.toString(), extractedUserId, "Extracted userId should match");

  }

  @Test
  @DisplayName("generateRefreshToken: Should generate a valid refresh token with correct subject")
  void generateRefreshToken_ShouldReturnValidToken() {

    // Act
    String refreshToken = jwtService.generateRefreshToken(testUserId, testUsername);

    // Assert
    assertNotNull(refreshToken, "Refresh token should not be null");
    assertEquals(testUsername, jwtService.extractUsername(refreshToken), "Extracted username should match");

  }

  @Test
  @DisplayName("isValid: Should return true when token matches user and is not expired")
  void isValid_ShouldReturnTrue_WhenTokenAndUserMatch() {

    // Arrange
    String token = jwtService.generateAccessToken(testUserId, testUsername);

    // Using your actual User entity instead of a mock
    User dummyUser = new User();
    dummyUser.setUsername(testUsername);

    // Act
    boolean isValid = jwtService.isValid(token, dummyUser);

    // Assert
    assertTrue(isValid, "Token should be valid for the matching user");

  }

  @Test
  @DisplayName("isValid: Should return false when token username does not match UserDetails")
  void isValid_ShouldReturnFalse_WhenUsernameDiffers() {

    // Arrange
    String token = jwtService.generateAccessToken(testUserId, testUsername);

    User dummyUser = new User();
    dummyUser.setUsername("imposter_user");

    // Act
    boolean isValid = jwtService.isValid(token, dummyUser);

    // Assert
    assertFalse(isValid, "Token should be invalid for a non-matching user");

  }

  @Test
  @DisplayName("getJwks: Should return a correctly formatted JSON Web Key Map")
  @SuppressWarnings("unchecked")
  void getJwks_ShouldReturnCorrectRsaKeys() {

    // Act
    Map<String, Object> jwksMap = (Map<String, Object>) jwtService.getJwks();

    // Assert
    assertTrue(jwksMap.containsKey("keys"), "JWKS map should contain 'keys'");

    List<Map<String, Object>> keys = (List<Map<String, Object>>) jwksMap.get("keys");
    assertEquals(1, keys.size(), "There should be exactly one key in the list");

    Map<String, Object> key = keys.get(0);
    assertEquals("RSA", key.get("kty"), "Key type should be RSA");
    assertEquals("auth-service-key", key.get("kid"), "Key ID should match the stable ID");
    assertNotNull(key.get("n"), "Modulus should not be null");
    assertNotNull(key.get("e"), "Exponent should not be null");

  }

  @Test
  @DisplayName("init: Should throw RuntimeException when PEM strings are invalid")
  void init_ShouldThrowException_WhenPemInvalid() {

    JwtServiceImpl failingService = new JwtServiceImpl();
    failingService.setPrivateKeyPem("this_is_not_a_valid_private_key");
    failingService.setPublicKeyPem("this_is_not_a_valid_public_key");

    RuntimeException exception = assertThrows(RuntimeException.class, failingService::init);

    assertEquals("Failed to initialize JwtService", exception.getMessage());

  }

}
