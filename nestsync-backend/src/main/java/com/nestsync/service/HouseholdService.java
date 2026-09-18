package com.nestsync.service;

import com.nestsync.dto.Responses;
import com.nestsync.model.*;
import com.nestsync.repository.*;
import java.math.BigDecimal;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
@Transactional
public class HouseholdService {
  private final CurrentUser current;
  private final UserRepository users;
  private final GroupRepository groups;
  private final ExpenseRepository expenses;
  private final ChoreRepository chores;
  private final GroceryRepository groceries;
  private final MaintenanceRepository maintenance;

  @Transactional(readOnly = true)
  public RoommateGroup home() {
    return current.get().getHousehold();
  }

  @Transactional(readOnly = true)
  public List<Responses.Member> members() {
    return users.findAllByHouseholdGroupIdOrderByUserId(home().getGroupId()).stream()
        .map(Responses.Member::from)
        .toList();
  }

  public RoommateGroup join(String inviteCode) {
    var user = current.get();
    var old = user.getHousehold();
    var target =
        groups
            .findByInviteCode(inviteCode.strip())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invite code not found."));
    if (target.getGroupId().equals(old.getGroupId())) return old;
    Long id = old.getGroupId();
    if (users.findAllByHouseholdGroupIdOrderByUserId(id).size() != 1
        || expenses.existsByGroupGroupId(id)
        || chores.existsByGroupGroupId(id)
        || groceries.existsByGroupGroupId(id)
        || maintenance.existsByGroupGroupId(id))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT,
          "Join a home before adding records or inviting roommates. Leaving an active home is not"
              + " supported yet.");
    user.setHousehold(target);
    users.saveAndFlush(user);
    groups.delete(old);
    return target;
  }

  @Transactional(readOnly = true)
  public List<Responses.Balance> balances() {
    var members = members();
    Map<Long, BigDecimal> totals = new LinkedHashMap<>();
    members.forEach(m -> totals.put(m.userId(), new BigDecimal("0.00")));
    for (var expense : expenses.findAllByGroupGroupIdOrderByExpenseIdDesc(home().getGroupId())) {
      totals.merge(expense.getPaidBy().getUserId(), expense.getAmount(), BigDecimal::add);
      expense.getShares().forEach((id, share) -> totals.merge(id, share.negate(), BigDecimal::add));
    }
    return members.stream()
        .map(m -> new Responses.Balance(m.userId(), m.name(), totals.get(m.userId())))
        .toList();
  }
}
