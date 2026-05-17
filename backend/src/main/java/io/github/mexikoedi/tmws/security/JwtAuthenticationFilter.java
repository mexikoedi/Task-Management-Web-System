package io.github.mexikoedi.tmws.security;

import io.github.mexikoedi.tmws.model.User;
import io.github.mexikoedi.tmws.repository.UserRepository;
import io.github.mexikoedi.tmws.util.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** JWT Authentication Filter - validiert JWT-Tokens in Authorization Header */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final UserRepository userRepository;
  private final JwtTokenProvider jwtTokenProvider;

  public JwtAuthenticationFilter(UserRepository userRepository, JwtTokenProvider jwtTokenProvider) {
    this.userRepository = userRepository;
    this.jwtTokenProvider = jwtTokenProvider;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    try {
      // Extract JWT token from Authorization header
      String token = extractTokenFromRequest(request);

        if (token != null) {
          if (!jwtTokenProvider.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
          }

          Claims claims = jwtTokenProvider.getClaims(token);
          String email = claims.getSubject();
          Integer tokenVersion = claims.get("tokenVersion", Integer.class);

          User user = userRepository.findByEmail(email).orElse(null);

          if (user == null || user.getTokenVersion() != tokenVersion) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
          }

          UsernamePasswordAuthenticationToken authentication =
              new UsernamePasswordAuthenticationToken(email, null, null);
          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
    } catch (Exception e) {
      logger.debug("Could not authenticate JWT token: " + e.getMessage());
    }

    filterChain.doFilter(request, response);
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}
