package com.nestsync.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

/** Splits integer cents, assigning remainder cents to the lowest participant IDs. */
public final class EqualSplitCalculator {
  private EqualSplitCalculator() {}

  public static Map<Long, BigDecimal> split(BigDecimal amount, List<Long> participantIds) {
    if (amount == null
        || amount.signum() <= 0
        || participantIds == null
        || participantIds.isEmpty()
        || participantIds.stream().anyMatch(Objects::isNull)
        || new HashSet<>(participantIds).size() != participantIds.size())
      throw new IllegalArgumentException(
          "A positive amount and distinct participants are required.");
    long cents = amount.setScale(2, RoundingMode.UNNECESSARY).movePointRight(2).longValueExact();
    var ids = participantIds.stream().sorted().toList();
    long each = cents / ids.size(), remainder = cents % ids.size();
    Map<Long, BigDecimal> shares = new LinkedHashMap<>();
    for (int i = 0; i < ids.size(); i++)
      shares.put(ids.get(i), BigDecimal.valueOf(each + (i < remainder ? 1 : 0), 2));
    return shares;
  }
}
