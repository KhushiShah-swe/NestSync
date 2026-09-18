package com.nestsync.security;

import static org.assertj.core.api.Assertions.*;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.Test;

class JwtUtilTest {
  private final String secret = "test-only-secret-longer-than-thirty-two-bytes";

  @Test
  void validatesSignedSubject() {
    var jwt = new JwtUtil(secret, 60000, false);
    assertThat(jwt.extractEmail(jwt.generateToken("alex@example.com")))
        .isEqualTo("alex@example.com");
  }

  @Test
  void rejectsTokensFromAnotherKey() {
    var jwt = new JwtUtil(secret, 60000, false);
    var attacker = new JwtUtil("different-test-secret-longer-than-thirty-two-bytes", 60000, false);
    assertThatThrownBy(() -> jwt.extractEmail(attacker.generateToken("alex@example.com")))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void rejectsExpiredTokens() {
    var jwt = new JwtUtil(secret, -10000, false);
    assertThatThrownBy(() -> jwt.extractEmail(jwt.generateToken("alex@example.com")))
        .isInstanceOf(JwtException.class);
  }

  @Test
  void rejectsMissingOrWeakProductionSecrets() {
    assertThatThrownBy(() -> new JwtUtil("", 60000, false))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> new JwtUtil("short", 60000, false))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void ephemeralDevelopmentKeysDifferAcrossBoots() {
    var first = new JwtUtil("", 60000, true);
    var second = new JwtUtil("", 60000, true);
    var token = first.generateToken("alex@example.com");
    assertThat(first.extractEmail(token)).isEqualTo("alex@example.com");
    assertThatThrownBy(() -> second.extractEmail(token)).isInstanceOf(JwtException.class);
  }
}
