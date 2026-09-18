package com.nestsync.service;

import com.nestsync.model.User;
import com.nestsync.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class CurrentUser {
  private final UserRepository users;

  public User get() {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null)
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in to continue.");
    return users
        .findByEmail(auth.getName())
        .orElseThrow(
            () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sign in to continue."));
  }
}
