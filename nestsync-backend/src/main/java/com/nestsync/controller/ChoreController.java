package com.nestsync.controller;

import com.nestsync.dto.Requests;
import com.nestsync.model.Chore;
import com.nestsync.service.ChoreService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chores")
@RequiredArgsConstructor
public class ChoreController {
  private final ChoreService service;

  @GetMapping
  public List<Chore> all() {
    return service.all();
  }

  @GetMapping("/{id}")
  public Chore get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Chore create(@Valid @RequestBody Requests.Chore request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public Chore update(@PathVariable Long id, @Valid @RequestBody Requests.Chore request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
