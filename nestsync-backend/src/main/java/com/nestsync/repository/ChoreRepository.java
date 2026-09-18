package com.nestsync.repository;

import com.nestsync.model.Chore;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChoreRepository extends JpaRepository<Chore, Long> {
  List<Chore> findAllByGroupGroupIdOrderByChoreIdDesc(Long groupId);

  Optional<Chore> findByChoreIdAndGroupGroupId(Long id, Long groupId);

  boolean existsByGroupGroupId(Long groupId);
}
