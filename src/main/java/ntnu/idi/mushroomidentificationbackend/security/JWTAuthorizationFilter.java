package ntnu.idi.mushroomidentificationbackend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

public class JWTAuthorizationFilter extends OncePerRequestFilter {

  private final JWTUtil jwtUtil;

  public JWTAuthorizationFilter(JWTUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String token = extractToken(request);

    if (token != null && jwtUtil.isTokenValid(token, jwtUtil.extractUsername(token))) {
      String username = jwtUtil.extractUsername(token);
      String role = jwtUtil.extractRole(token);

      User user = new User(username, "", Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role)));
      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
          user, null, user.getAuthorities()
      );

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    chain.doFilter(request, response);
  }

  /**
   * Extracts JWT token from Authorization header.
   */
  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : null;
  }
}
