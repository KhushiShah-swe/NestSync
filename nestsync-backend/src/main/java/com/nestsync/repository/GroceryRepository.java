package com.nestsync.repository;

import com.nestsync.model.GroceryItem;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroceryRepository extends JpaRepository<GroceryItem, Long> {
  List<GroceryItem> findAllByGroupGroupIdOrderByGroceryIdDesc(Long groupId);

  Optional<GroceryItem> findByGroceryIdAndGroupGroupId(Long id, Long groupId);

  boolean existsByGroupGroupId(Long groupId);
}
