package com.kitnets.user;

import com.kitnets.dto.AuthDtos;
import com.kitnets.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final UserRepository users;
  private final PasswordEncoder encoder;
  private final AuthenticationManager authManager;
  private final JwtUtil jwt;

  public AuthController(UserRepository users, PasswordEncoder encoder, AuthenticationManager authManager, JwtUtil jwt) {
    this.users = users;
    this.encoder = encoder;
    this.authManager = authManager;
    this.jwt = jwt;
  }

  @PostMapping("/register")
  public ResponseEntity<?> register(@Valid @RequestBody AuthDtos.RegisterRequest req) {
    if (users.existsByEmail(req.email)) {
      return ResponseEntity.badRequest().body("Email já cadastrado.");
    }
    if (users.existsByCpf(req.cpf)) {
      return ResponseEntity.badRequest().body("CPF já cadastrado.");
    }

    User u = new User();
    u.setName(req.name);
    u.setEmail(req.email.toLowerCase());
    u.setPasswordHash(encoder.encode(req.password));
    u.setPhone(req.phone);
    u.setCpf(req.cpf);
    u.setIncome(req.income);
    u.setRole(User.Role.USER);

    users.save(u);

    return ResponseEntity.ok().build();
  }

  @PostMapping("/login")
  public ResponseEntity<?> login(@Valid @RequestBody AuthDtos.LoginRequest req) {
    Authentication auth = authManager.authenticate(
    new UsernamePasswordAuthenticationToken(req.email.toLowerCase(), req.password)
);

SecurityContextHolder.getContext().setAuthentication(auth);

    User u = users.findByEmail(req.email.toLowerCase()).orElseThrow();
    String token = jwt.generateToken(u.getEmail(), u.getRole().name());

    AuthDtos.UserResponse ur = new AuthDtos.UserResponse();
    ur.id = u.getId();
    ur.name = u.getName();
    ur.email = u.getEmail();
    ur.role = u.getRole().name();

    AuthDtos.LoginResponse lr = new AuthDtos.LoginResponse();
    lr.token = token;
    lr.user = ur;

    return ResponseEntity.ok(lr);
  }
}
