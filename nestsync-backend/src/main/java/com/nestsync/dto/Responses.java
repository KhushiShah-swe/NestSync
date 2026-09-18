package com.nestsync.dto;

import com.nestsync.model.User;
import java.math.BigDecimal;

public final class Responses {
  private Responses() {}

  public record Member(Long userId, String name, String email) {
    public static Member from(User user) {
      return new Member(user.getUserId(), user.getName(), user.getEmail());
    }
  }

  public record Auth(String token, Member user) {}

  public record Balance(Long userId, String name, BigDecimal netAmount) {}
}
