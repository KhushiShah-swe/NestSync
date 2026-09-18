package com.nestsync.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class GroceryItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long groceryId;

  private String itemName;
  private Integer quantity;
  private Boolean purchased;

  @JsonIgnore
  @ManyToOne(optional = false)
  @JoinColumn(name = "household_id")
  private RoommateGroup group;
}
