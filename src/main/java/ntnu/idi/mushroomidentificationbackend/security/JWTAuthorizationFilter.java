package ntnu.idi.mushroomidentificationbackend.security;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.logging.Logger;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

/**
 * Filter for JWT authorization.
 */
public class JWTAuthorizationFilter extends OncePerRequestFilter {

  private final JWTUtil jwtUtil;
  private final Logger logger = Logger.getLogger(JWTAuthorizationFilter.class.getName());

  public JWTAuthorizationFilter(JWTUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  /**
   * Processes the incoming HTTP request to check for a JWT token.
   * If a valid token is found, it extracts the username and role,
   * creates an authentication object,
   * and sets it in the SecurityContext.
   * If the token is invalid or expired,
   * it sends an error response with the appropriate status code.
   * This method is called for every request to the application,
   * allowing for centralized authentication handling.
   *
   * @param request the HTTP request containing the JWT token in the Authorization header
   * @param response the HTTP response to send back in case of errors
   * @param chain the filter chain to continue processing the request
   * @throws ServletException if an error occurs during request processing
   * @throws IOException if an I/O error occurs during request processing
   */
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {

    String token = extractToken(request);

    if (token != null) {
      try {
        if (jwtUtil.isTokenValid(token)) {
          String username = jwtUtil.extractUsername(token);
          String role = jwtUtil.extractRole(token);

          User user = new User(username, "", Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role)));
          UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
              user, null, user.getAuthorities()
          );

          SecurityContextHolder.getContext().setAuthentication(authentication);
        }
      } catch (ExpiredJwtException e) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has expired");
        return;
      } catch (MalformedJwtException e) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format");
        return;
      } catch (Exception e) {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized request");
        return;
      }
    }

    chain.doFilter(request, response);
  }

  /**
   * Extracts the JWT token from the Authorization header of the request.
   *
   * @param request the HTTP request containing the Authorization header
   * @return the extracted token, or null if not found or invalid
   */
  private String extractToken(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.startsWith("Bearer ")) {
      String token = header.substring(7).trim();
      return token.isEmpty() ? null : token;
    }
    return null;
  }
}
