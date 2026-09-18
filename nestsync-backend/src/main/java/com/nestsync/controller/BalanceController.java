package com.nestsync.controller;

import com.nestsync.dto.Responses;
import com.nestsync.service.HouseholdService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/balances")
@RequiredArgsConstructor
public class BalanceController {
  private final HouseholdService service;

  @GetMapping
  public List<Responses.Balance> balances() {
    return service.balances();
  }
}
