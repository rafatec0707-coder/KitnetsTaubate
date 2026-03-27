package com.kitnets.security;

import com.kitnets.user.User;
import com.kitnets.user.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;

  public JwtAuthFilter(JwtUtil jwtUtil, UserRepository userRepository) {
    this.jwtUtil = jwtUtil;
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // Preflight não precisa de auth
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      filterChain.doFilter(request, response);
      return;
    }

    String authHeader = request.getHeader("Authorization");
    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);

      try {
        Claims claims = jwtUtil.parse(token).getBody();
        String email = claims.getSubject();

        // Pega o role REAL do banco (evita 403 por claim errado)
        User u = userRepository.findByEmail(email).orElse(null);
        if (u != null) {
          String role = u.getRole().name().trim().toUpperCase(); // ADMIN / USER
          var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

          var auth = new UsernamePasswordAuthenticationToken(email, null, authorities);
          SecurityContextHolder.getContext().setAuthentication(auth);
        }
      } catch (Exception ignored) {
        // token inválido: deixa passar sem autenticar (vai cair em 401/403 nos endpoints protegidos)
      }
    }

    filterChain.doFilter(request, response);
  }
}