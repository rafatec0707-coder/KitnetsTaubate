package com.kitnets.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalTime;

public class VisitDtos {

  public static class CreateVisitRequest {
    @NotNull
    public LocalDate visitDate;

    @NotNull
    public LocalTime visitTime;

    @Size(max=500)
    public String message;
  }

  public static class VisitResponse {
    public Long id;
    public LocalDate visitDate;
    public LocalTime visitTime;
    public String status;
    public String message;
    public String createdAt;
    public String userName;  // for admin listing
    public String userEmail; // for admin listing
  }

  public static class UpdateStatusRequest {
    @NotBlank
    public String status; // PENDING/APPROVED/REJECTED/CANCELED
  }

  public static class StatsResponse {
    public long total;
    public long pending;
    public long approved;
    public long rejected;
  }
}
