package com.nestsync.service;

import com.nestsync.dto.Requests;
import com.nestsync.model.Expense;
import com.nestsync.model.User;
import com.nestsync.repository.*;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseService {
  private final ExpenseRepository expenses;
  private final UserRepository users;
  private final CurrentUser current;

  @Transactional(readOnly = true)
  public List<Expense> all() {
    return expenses.findAllByGroupGroupIdOrderByExpenseIdDesc(
        current.get().getHousehold().getGroupId());
  }

  @Transactional(readOnly = true)
  public Expense get(Long id) {
    return expenses
        .findByExpenseIdAndGroupGroupId(id, current.get().getHousehold().getGroupId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Expense not found."));
  }

  public Expense create(Requests.Expense request) {
    User user = current.get();
    var expense = new Expense();
    expense.setGroup(user.getHousehold());
    expense.setPaidBy(user);
    var participants =
        users.findAllByHouseholdGroupIdOrderByUserId(user.getHousehold().getGroupId()).stream()
            .map(User::getUserId)
            .toList();
    apply(expense, request);
    expense.setShares(EqualSplitCalculator.split(request.amount(), participants));
    return expenses.save(expense);
  }

  public Expense update(Long id, Requests.Expense request) {
    var expense = get(id);
    apply(expense, request);
    // Preserve the original participant set after new roommates join.
    expense.setShares(
        EqualSplitCalculator.split(request.amount(), List.copyOf(expense.getShares().keySet())));
    return expenses.save(expense);
  }

  public void delete(Long id) {
    expenses.delete(get(id));
  }

  private void apply(Expense expense, Requests.Expense request) {
    expense.setTitle(request.title().strip());
    expense.setAmount(request.amount());
    expense.setDate(request.date());
    expense.setCategory(request.category().strip());
    expense.setNotes(request.notes());
    expense.setSplitType(request.splitType());
  }
}
