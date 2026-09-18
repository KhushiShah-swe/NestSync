package com.nestsync.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Chore {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long choreId;

  private String choreName;
  private java.time.LocalDate dueDate;
  private String recurrence;
  private String status;
  @ManyToOne private User assignee;

  @JsonIgnore
  @ManyToOne(optional = false)
  @JoinColumn(name = "household_id")
  private RoommateGroup group;
}
