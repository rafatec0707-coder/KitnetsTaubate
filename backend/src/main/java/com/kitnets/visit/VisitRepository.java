package com.kitnets.visit;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepository extends JpaRepository<Visit, Long> {
  List<Visit> findByUserIdOrderByCreatedAtDesc(Long userId);
  long countByStatus(Visit.Status status);
}
