package com.nestsync.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtUtil {
  private final Key key;
  private final long ttl;

  public JwtUtil(
      @Value("${nestsync.jwt.secret}") String secret,
      @Value("${nestsync.jwt.ttl-ms}") long ttl,
      @Value("${nestsync.jwt.allow-ephemeral}") boolean allowEphemeral) {
    if (secret.isBlank() && allowEphemeral) key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    else {
      if (secret.getBytes(StandardCharsets.UTF_8).length < 32)
        throw new IllegalArgumentException(
            "JWT_SECRET must contain at least 32 bytes outside the dev profile.");
      key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
    this.ttl = ttl;
  }

  public String generateToken(String email) {
    return Jwts.builder()
        .setIssuer("nestsync")
        .setSubject(email)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + ttl))
        .signWith(key)
        .compact();
  }

  public String extractEmail(String token) {
    return Jwts.parserBuilder()
        .requireIssuer("nestsync")
        .setSigningKey(key)
        .build()
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }
}
