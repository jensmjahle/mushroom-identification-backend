package ntnu.idi.mushroomidentificationbackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


/**
 * Configures security settings for JWT authentication.
 */
@Configuration
public class SecurityConfig {

  private final JWTUtil jwtUtil;
  private final TokenBlocklistService tokenBlocklistService;
  private static final String SUPERUSER_ROLE = "SUPERUSER";
  private static final String MODERATOR_ROLE = "MODERATOR";
  private static final String USER_ROLE = "USER";


  public SecurityConfig(JWTUtil jwtUtil, TokenBlocklistService tokenBlocklistService) {
    this.jwtUtil = jwtUtil;
    this.tokenBlocklistService = tokenBlocklistService;
  }

  /**
   * Configures the security filter chain for the application.
   * This method sets up the HTTP security configuration,
   * including CSRF protection, authorization rules,
   * and the JWT authorization filter.
   *
   * @param http the HttpSecurity object to configure
   * @return a SecurityFilterChain object that defines the security configuration
   * @throws Exception if an error occurs during configuration
   */
  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authz -> authz
            // Spring Boot's error controller is reached via an internal Tomcat
            // dispatch (RequestDispatcher.include) whenever a filter/handler calls
            // sendError() or an exception escapes normal MVC handling - it re-enters
            // this same filter chain, without the original request's role in scope.
            // Without this rule the error dispatch itself gets denied, so callers see
            // a generic 403 instead of the actual underlying error (401, 400, 500...).
            .requestMatchers("/error").permitAll()
            .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
            .requestMatchers("/auth/admin/login").permitAll()
            .requestMatchers("/auth/user/login").permitAll()
            .requestMatchers("/api/requests/**").permitAll()
            .requestMatchers("/api/images/**").permitAll()
            .requestMatchers("/api/websocket/**").permitAll()
            .requestMatchers("/api/stats/**").permitAll()
            // More specific matchers must be evaluated before the broader /api/** and
            // /admin/** rules below, since Spring Security applies only the first match.
            .requestMatchers("/admin/superuser/**").hasRole(SUPERUSER_ROLE)
            .requestMatchers("/api/admin/requests/*/release").hasRole(SUPERUSER_ROLE)
            .requestMatchers("/api/admin/**").hasAnyRole(SUPERUSER_ROLE, MODERATOR_ROLE)
            .requestMatchers("/admin/**").hasAnyRole(SUPERUSER_ROLE, MODERATOR_ROLE)
            .requestMatchers("/api/**").hasAnyRole(SUPERUSER_ROLE, MODERATOR_ROLE, USER_ROLE)
            .requestMatchers("/ws/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(new JWTAuthorizationFilter(jwtUtil, tokenBlocklistService),
            UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  /**
   * Bean for password encoding.
   * This method provides a BCryptPasswordEncoder instance
   * which is used to encode passwords securely.
   *
   * @return a PasswordEncoder instance
   */
  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
