package com.nestsync.service;

import com.nestsync.dto.Requests;
import com.nestsync.model.Chore;
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
public class ChoreService {
  private final ChoreRepository records;
  private final CurrentUser current;
  private final UserRepository users;

  @Transactional(readOnly = true)
  public List<Chore> all() {
    return records.findAllByGroupGroupIdOrderByChoreIdDesc(
        current.get().getHousehold().getGroupId());
  }

  @Transactional(readOnly = true)
  public Chore get(Long id) {
    return records
        .findByChoreIdAndGroupGroupId(id, current.get().getHousehold().getGroupId())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Chore record not found."));
  }

  public Chore create(Requests.Chore request) {
    var entity = new Chore();
    entity.setGroup(current.get().getHousehold());
    apply(entity, request);
    return records.save(entity);
  }

  public Chore update(Long id, Requests.Chore request) {
    var entity = get(id);
    apply(entity, request);
    return records.save(entity);
  }

  public void delete(Long id) {
    records.delete(get(id));
  }

  private void apply(Chore entity, Requests.Chore request) {
    entity.setChoreName(request.choreName().strip());
    entity.setDueDate(request.dueDate());
    entity.setRecurrence(request.recurrence());
    entity.setStatus(request.status());
    entity.setAssignee(
        request.assigneeId() == null
            ? null
            : users
                .findById(request.assigneeId())
                .filter(u -> u.getHousehold().getGroupId().equals(entity.getGroup().getGroupId()))
                .orElseThrow(
                    () ->
                        new ResponseStatusException(
                            HttpStatus.BAD_REQUEST, "Assignee must belong to this home.")));
  }
}
