package com.nestsync.service;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EqualSplitCalculatorTest {
  @ParameterizedTest
  @CsvSource({"10.00,3", "0.01,4", "99.99,7", "100.00,1", "9999999999.99,3"})
  void conservesEveryCent(String amount, int count) {
    var ids = new ArrayList<Long>();
    for (long i = 1; i <= count; i++) ids.add(i);
    var shares = EqualSplitCalculator.split(new BigDecimal(amount), ids);
    assertThat(shares.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add))
        .isEqualByComparingTo(amount);
    assertThat(shares).hasSize(count);
    assertThat(shares.values()).allSatisfy(value -> assertThat(value.scale()).isEqualTo(2));
    var min = Collections.min(shares.values());
    var max = Collections.max(shares.values());
    assertThat(max.subtract(min)).isLessThanOrEqualTo(new BigDecimal("0.01"));
  }

  @Test
  void distributesRemaindersDeterministically() {
    var shares = EqualSplitCalculator.split(new BigDecimal("10.00"), List.of(3L, 1L, 2L));
    assertThat(shares)
        .containsEntry(1L, new BigDecimal("3.34"))
        .containsEntry(2L, new BigDecimal("3.33"))
        .containsEntry(3L, new BigDecimal("3.33"));
  }

  @Test
  void rejectsDuplicateOrMissingParticipants() {
    for (var ids : List.of(List.<Long>of(), List.of(1L, 1L), Arrays.asList(1L, null)))
      assertThatThrownBy(() -> EqualSplitCalculator.split(BigDecimal.ONE, ids))
          .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> EqualSplitCalculator.split(BigDecimal.ONE, null))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void rejectsNonPositiveOrMissingAmounts() {
    for (var amount : Arrays.asList(BigDecimal.ZERO, new BigDecimal("-1"), null))
      assertThatThrownBy(() -> EqualSplitCalculator.split(amount, List.of(1L)))
          .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void refusesSilentRounding() {
    assertThatThrownBy(() -> EqualSplitCalculator.split(new BigDecimal("1.001"), List.of(1L)))
        .isInstanceOf(ArithmeticException.class);
  }
}
