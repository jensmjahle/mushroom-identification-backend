package ntnu.idi.mushroomidentificationbackend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
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

  public SecurityConfig(JWTUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable) 
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/auth/admin/login").permitAll()
            .requestMatchers("/auth/user/login").permitAll()
            .requestMatchers("/api/requests/**").permitAll()
            .requestMatchers("/admin/**").hasAnyRole("SUPERUSER", "MODERATOR")
            .requestMatchers("/admin/superuser/**").hasRole("SUPERUSER")
            .requestMatchers("/ws/**").permitAll()
            .anyRequest().authenticated()
        )
        .addFilterBefore(new JWTAuthorizationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

    return http.build();
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
