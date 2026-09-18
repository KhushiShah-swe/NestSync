package com.nestsync.repository;

import com.nestsync.model.Expense;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
  List<Expense> findAllByGroupGroupIdOrderByExpenseIdDesc(Long groupId);

  Optional<Expense> findByExpenseIdAndGroupGroupId(Long id, Long groupId);

  boolean existsByGroupGroupId(Long groupId);
}
