package com.kitnets.visit;

import com.kitnets.dto.VisitDtos;
import com.kitnets.user.User;
import com.kitnets.user.UserRepository;
import jakarta.validation.Valid;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

  private final VisitRepository visits;
  private final UserRepository users;

  public VisitController(VisitRepository visits, UserRepository users) {
    this.visits = visits;
    this.users = users;
  }

  private User currentUser(Authentication auth) {
    String email = String.valueOf(auth.getPrincipal());
    return users.findByEmail(email).orElseThrow();
  }

  @PostMapping
  public ResponseEntity<?> create(Authentication auth, @Valid @RequestBody VisitDtos.CreateVisitRequest req) {
    User u = currentUser(auth);

    Visit v = new Visit();
    v.setUser(u);
    v.setVisitDate(req.visitDate);
    v.setVisitTime(req.visitTime);
    v.setMessage(req.message);
    v.setStatus(Visit.Status.PENDING);

    visits.save(v);
    return ResponseEntity.ok(toDto(v, true));
  }

  @GetMapping("/mine")
  public ResponseEntity<?> mine(Authentication auth) {
    User u = currentUser(auth);
    List<Visit> list = visits.findByUserIdOrderByCreatedAtDesc(u.getId());
    return ResponseEntity.ok(list.stream().map(v -> toDto(v, false)).toList());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @GetMapping
  public ResponseEntity<?> all() {
    return ResponseEntity.ok(visits.findAll().stream().map(v -> toDto(v, true)).toList());
  }

  @PreAuthorize("hasRole('ADMIN')")
  @RequestMapping(value = "/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
  public ResponseEntity<?> updateStatus(@PathVariable("id") Long id,
                                       @Valid @RequestBody VisitDtos.UpdateStatusRequest req) {

  // DEBUG: ver quem o Spring acha que você é e quais roles chegaram
  Authentication a = org.springframework.security.core.context.SecurityContextHolder
      .getContext().getAuthentication();
  System.out.println("AUTH=" + a);
  System.out.println("ROLES=" + (a == null ? "null" : a.getAuthorities()));

    Visit v = visits.findById(id).orElse(null);
    if (v == null) return ResponseEntity.notFound().build();

    try {
      v.setStatus(Visit.Status.valueOf(req.status));
    } catch (Exception e) {
      return ResponseEntity.badRequest().body("Status inválido.");
    }

    visits.save(v);
    return ResponseEntity.ok(toDto(v, true));
  }

 @GetMapping("/stats")
  @PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> stats() {
  VisitDtos.StatsResponse s = new VisitDtos.StatsResponse();
  s.total = visits.count();
  s.pending = visits.countByStatus(Visit.Status.PENDING);
  s.approved = visits.countByStatus(Visit.Status.APPROVED);
  s.rejected = visits.countByStatus(Visit.Status.REJECTED);
  return ResponseEntity.ok(s);
}

  private VisitDtos.VisitResponse toDto(Visit v, boolean includeUser) {
    VisitDtos.VisitResponse r = new VisitDtos.VisitResponse();
    r.id = v.getId();
    r.visitDate = v.getVisitDate();
    r.visitTime = v.getVisitTime();
    r.status = v.getStatus().name();
    r.message = v.getMessage();
    r.createdAt = v.getCreatedAt().toString();
    if (includeUser) {
      r.userName = v.getUser().getName();
      r.userEmail = v.getUser().getEmail();
    }
    return r;
  }
}