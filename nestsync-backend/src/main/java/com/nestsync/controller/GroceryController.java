package com.nestsync.controller;

import com.nestsync.dto.Requests;
import com.nestsync.model.GroceryItem;
import com.nestsync.service.GroceryService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groceries")
@RequiredArgsConstructor
public class GroceryController {
  private final GroceryService service;

  @GetMapping
  public List<GroceryItem> all() {
    return service.all();
  }

  @GetMapping("/{id}")
  public GroceryItem get(@PathVariable Long id) {
    return service.get(id);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public GroceryItem create(@Valid @RequestBody Requests.Grocery request) {
    return service.create(request);
  }

  @PutMapping("/{id}")
  public GroceryItem update(@PathVariable Long id, @Valid @RequestBody Requests.Grocery request) {
    return service.update(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id) {
    service.delete(id);
  }
}
