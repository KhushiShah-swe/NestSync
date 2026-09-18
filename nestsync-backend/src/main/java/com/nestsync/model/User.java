package com.nestsync.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long userId;

  @Column(nullable = false, length = 80)
  private String name;

  @Column(unique = true, nullable = false, length = 254)
  private String email;

  @JsonIgnore
  @Column(nullable = false)
  private String password;

  @JsonIgnore
  @ManyToOne(optional = false)
  @JoinColumn(name = "household_id")
  private RoommateGroup household;
}
