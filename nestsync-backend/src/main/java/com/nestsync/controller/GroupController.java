package com.nestsync.controller;

import com.nestsync.dto.*;
import com.nestsync.model.RoommateGroup;
import com.nestsync.service.HouseholdService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {
  private final HouseholdService service;

  @GetMapping("/me")
  public RoommateGroup home() {
    return service.home();
  }

  @GetMapping("/members")
  public List<Responses.Member> members() {
    return service.members();
  }

  @PostMapping("/join")
  public RoommateGroup join(@Valid @RequestBody Requests.Join request) {
    return service.join(request.inviteCode());
  }
}
