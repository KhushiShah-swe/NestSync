package com.nestsync.controller;

import com.nestsync.dto.Requests;
import com.nestsync.model.MaintenanceIssue;
import com.nestsync.service.MaintenanceService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {
  private final MaintenanceService service;

  @GetMapping
  public List<MaintenanceIssue> all() {
    return service.all();
  }

  @GetMapping("/{id}")
  public MaintenanceIssue get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public MaintenanceIssue create(@Valid @RequestBody Requests.Maintenance request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public MaintenanceIssue update(
      @PathVariable Long id, @Valid @RequestBody Requests.Maintenance request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
