package com.nestsync.repository;

import com.nestsync.model.RoommateGroup;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GroupRepository extends JpaRepository<RoommateGroup, Long> {
  Optional<RoommateGroup> findByInviteCode(String inviteCode);
}
