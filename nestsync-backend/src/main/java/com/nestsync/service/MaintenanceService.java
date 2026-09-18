package com.nestsync.service;

import com.nestsync.dto.Requests;
import com.nestsync.model.MaintenanceIssue;
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
public class MaintenanceService {
  private final MaintenanceRepository records;
  private final CurrentUser current;

  @Transactional(readOnly = true)
  public List<MaintenanceIssue> all() {
    return records.findAllByGroupGroupIdOrderByIssueIdDesc(
        current.get().getHousehold().getGroupId());
  }

  @Transactional(readOnly = true)
  public MaintenanceIssue get(Long id) {
    return records
        .findByIssueIdAndGroupGroupId(id, current.get().getHousehold().getGroupId())
        .orElseThrow(
            () ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Maintenance record not found."));
  }

  public MaintenanceIssue create(Requests.Maintenance request) {
    var entity = new MaintenanceIssue();
    entity.setGroup(current.get().getHousehold());
    apply(entity, request);
    return records.save(entity);
  }

  public MaintenanceIssue update(Long id, Requests.Maintenance request) {
    var entity = get(id);
    apply(entity, request);
    return records.save(entity);
  }

  public void delete(Long id) {
    records.delete(get(id));
  }

  private void apply(MaintenanceIssue entity, Requests.Maintenance request) {
    entity.setTitle(request.title().strip());
    entity.setDescription(request.description().strip());
    entity.setStatus(request.status());
  }
}
