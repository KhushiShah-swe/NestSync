package com.nestsync.controller;

import com.nestsync.dto.Requests;
import com.nestsync.model.Expense;
import com.nestsync.service.ExpenseService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
  private final ExpenseService service;

  @GetMapping
  public List<Expense> all() {
    return service.all();
  }

  @GetMapping("/{id}")
  public Expense get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Expense create(@Valid @RequestBody Requests.Expense request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public Expense update(@PathVariable Long id, @Valid @RequestBody Requests.Expense request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
