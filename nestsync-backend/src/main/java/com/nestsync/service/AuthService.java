package com.nestsync.service;

import com.nestsync.dto.*;
import com.nestsync.model.*;
import com.nestsync.repository.*;
import com.nestsync.security.JwtUtil;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class AuthService {
  private final UserRepository users;
  private final GroupRepository groups;
  private final PasswordEncoder encoder;
  private final JwtUtil jwt;

  @Transactional
  public Responses.Auth register(Requests.Register request) {
    String email = request.email().strip().toLowerCase(Locale.ROOT);
    if (users.existsByEmail(email))
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "An account with this email already exists.");
    if (request.password().getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "Password must be at most 72 UTF-8 bytes.");
    var home = new RoommateGroup();
    home.setGroupName(request.name().strip() + "'s home");
    home.setInviteCode(UUID.randomUUID().toString());
    var user = new User();
    user.setName(request.name().strip());
    user.setEmail(email);
    user.setPassword(encoder.encode(request.password()));
    user.setHousehold(groups.save(home));
    users.save(user);
    return new Responses.Auth(jwt.generateToken(email), Responses.Member.from(user));
  }

  @Transactional(readOnly = true)
  public Responses.Auth login(Requests.Login request) {
    User user = users.findByEmail(request.email().strip().toLowerCase(Locale.ROOT)).orElse(null);
    if (user == null || !encoder.matches(request.password(), user.getPassword()))
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Email or password is incorrect.");
    return new Responses.Auth(jwt.generateToken(user.getEmail()), Responses.Member.from(user));
  }
}
