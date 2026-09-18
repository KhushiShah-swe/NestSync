package com.nestsync.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class RoommateGroup {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long groupId;

  @Column(nullable = false, length = 120)
  private String groupName;

  @Column(nullable = false, unique = true, length = 36)
  private String inviteCode;
}
