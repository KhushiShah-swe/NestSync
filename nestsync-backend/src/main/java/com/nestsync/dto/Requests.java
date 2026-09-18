package com.nestsync.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public final class Requests {
  private Requests() {}

  public record Register(
      @NotBlank @Size(max = 80) String name,
      @NotBlank @Email @Size(max = 254) String email,
      @NotBlank @Size(min = 8, max = 64) String password) {}

  public record Login(
      @NotBlank @Email @Size(max = 254) String email, @NotBlank @Size(max = 64) String password) {}

  public record Expense(
      @NotBlank @Size(max = 120) String title,
      @NotNull @DecimalMin("0.01") @Digits(integer = 10, fraction = 2) BigDecimal amount,
      @NotNull LocalDate date,
      @NotBlank @Size(max = 40) String category,
      @Size(max = 1000) String notes,
      @NotNull @Pattern(regexp = "EQUAL") String splitType) {}

  public record Chore(
      @NotBlank @Size(max = 120) String choreName,
      @NotNull LocalDate dueDate,
      @NotNull @Pattern(regexp = "NONE|DAILY|WEEKLY|MONTHLY") String recurrence,
      @NotNull @Pattern(regexp = "TODO|IN_PROGRESS|DONE") String status,
      Long assigneeId) {}

  public record Grocery(
      @NotBlank @Size(max = 120) String itemName,
      @NotNull @Min(1) @Max(999) Integer quantity,
      @NotNull Boolean purchased) {}

  public record Maintenance(
      @NotBlank @Size(max = 120) String title,
      @NotBlank @Size(max = 2000) String description,
      @NotNull @Pattern(regexp = "OPEN|IN_PROGRESS|RESOLVED") String status) {}

  public record Join(@NotBlank @Size(max = 36) String inviteCode) {}
}
