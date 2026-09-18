package com.nestsync.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class MaintenanceIssue {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long issueId;

  private String title;

  @Column(length = 2000)
  private String description;

  private String status;

  @JsonIgnore
  @ManyToOne(optional = false)
  @JoinColumn(name = "household_id")
  private RoommateGroup group;
}
