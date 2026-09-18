package com.nestsync.controller;

import com.nestsync.dto.Responses;
import com.nestsync.service.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
  private final CurrentUser current;

  @GetMapping("/me")
  public Responses.Member me() {
    return Responses.Member.from(current.get());
  }
}
