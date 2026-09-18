package com.nestsync.controller;

import com.nestsync.dto.*;
import com.nestsync.service.AuthService;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@SecurityRequirements
public class AuthController {
  private final AuthService service;

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.CREATED)
  public Responses.Auth register(@Valid @RequestBody Requests.Register request) {
    return service.register(request);
  }

  @PostMapping("/login")
  public Responses.Auth login(@Valid @RequestBody Requests.Login request) {
    return service.login(request);
  }
}
