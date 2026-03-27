package com.kitnets.config;

import com.kitnets.user.User;
import com.kitnets.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

  @Bean
  CommandLineRunner seedAdmin(UserRepository users, PasswordEncoder encoder) {
    return args -> {
      String adminEmail = "admin@kitnets.local";
      if (users.findByEmail(adminEmail).isEmpty()) {
        User a = new User();
        a.setName("Admin");
        a.setEmail(adminEmail);
        a.setPasswordHash(encoder.encode("Admin123!"));
        a.setRole(User.Role.ADMIN);
        a.setPhone("000000000");
        a.setCpf("00000000000");
        a.setIncome(0.0);
        users.save(a);
        System.out.println("✅ Admin criado: " + adminEmail + " / senha: Admin123!");
      }
    };
  }
}
