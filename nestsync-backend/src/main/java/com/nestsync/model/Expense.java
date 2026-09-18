package com.nestsync.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Expense {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long expenseId;

  @Column(nullable = false, length = 120)
  private String title;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false)
  private LocalDate date;

  @Column(nullable = false, length = 40)
  private String category;

  @Column(length = 1000)
  private String notes;

  @Column(nullable = false)
  private String splitType = "EQUAL";

  @ManyToOne(optional = false)
  private User paidBy;

  @JsonIgnore
  @ManyToOne(optional = false)
  @JoinColumn(name = "household_id")
  private RoommateGroup group;

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "expense_shares", joinColumns = @JoinColumn(name = "expense_id"))
  @MapKeyColumn(name = "user_id")
  @Column(name = "share_amount", nullable = false, precision = 12, scale = 2)
  private Map<Long, BigDecimal> shares = new LinkedHashMap<>();
}
