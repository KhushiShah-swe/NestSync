package com.nestsync.repository;

import com.nestsync.model.MaintenanceIssue;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaintenanceRepository extends JpaRepository<MaintenanceIssue, Long> {
  List<MaintenanceIssue> findAllByGroupGroupIdOrderByIssueIdDesc(Long groupId);

  Optional<MaintenanceIssue> findByIssueIdAndGroupGroupId(Long id, Long groupId);

  boolean existsByGroupGroupId(Long groupId);
}
