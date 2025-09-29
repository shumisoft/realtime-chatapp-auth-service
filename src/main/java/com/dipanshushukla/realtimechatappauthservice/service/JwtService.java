package com.dipanshushukla.realtimechatappauthservice.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;

import io.jsonwebtoken.Claims;

public interface JwtService {

  String getPublicKeyPem();

  String getPrivateKeyPem();

  PrivateKey getPrivateKey();

  PublicKey getPublicKey();

  void setPublicKeyPem(String publicKeyPem);

  void setPrivateKeyPem(String privateKeyPem);

  void setPrivateKey(PrivateKey privateKey);

  void setPublicKey(PublicKey publicKey);

  boolean equals(java.lang.Object o);

  int hashCode();

  java.lang.String toString();

  void init();

  String extractUsername(String token);

  boolean isValid(String token, UserDetails user);

  <T> T extractClaim(String token, Function<Claims, T> resolver);

  String generateAccessToken(UUID userId, String username);

  String generateRefreshToken(UUID userId, String username);

  Object getJwks();

}