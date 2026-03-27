package com.kitnets.dto;

import jakarta.validation.constraints.*;

public class AuthDtos {

  public static class RegisterRequest {
    @NotBlank @Size(max=120)
    public String name;

    @NotBlank @Email @Size(max=180)
    public String email;

    @NotBlank @Size(min=6, max=100)
    public String password;

    @NotBlank @Size(max=30)
    public String phone;

    @NotBlank @Size(min=11, max=14)
    public String cpf;

    @NotNull @Positive
    public Double income;
  }

  public static class LoginRequest {
    @NotBlank @Email @Size(max=180)
    public String email;

    @NotBlank
    public String password;
  }

  public static class UserResponse {
    public Long id;
    public String name;
    public String email;
    public String role;
  }

  public static class LoginResponse {
    public String token;
    public UserResponse user;
  }
}
