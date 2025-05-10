package ntnu.idi.mushroomidentificationbackend.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;


/**
 * Configures security settings for JWT authentication.
 */
@Configuration
public class SecurityConfig {

  private final JWTUtil jwtUtil;
  private final CorsConfigurationSource corsConfigurationSource;
  private static final String SUPERUSER_ROLE = "SUPERUSER";
  private static final String MODERATOR_ROLE = "MODERATOR";
  private static final String USER_ROLE = "USER";


  public SecurityConfig(JWTUtil jwtUtil, CorsConfigurationSource corsConfigurationSource) {
    this.jwtUtil = jwtUtil;
    this.corsConfigurationSource = corsConfigurationSource;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        //.cors(withDefaults())
        .cors(AbstractHttpConfigurer::disable)
       // .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authz -> authz
            .requestMatchers("/auth/admin/login").permitAll()
            .requestMatchers("/auth/user/login").permitAll()
            .requestMatchers("/api/requests/**").permitAll()
            .requestMatchers("/api/images/**").permitAll()
            .requestMatchers("/api/websocket/**").permitAll()
            .requestMatchers("/api/**").hasAnyRole(SUPERUSER_ROLE, MODERATOR_ROLE, USER_ROLE)
            .requestMatchers("/api/admin/**").hasAnyRole(SUPERUSER_ROLE, MODERATOR_ROLE)
            .requestMatchers("/admin/**").hasAnyRole(SUPERUSER_ROLE, MODERATOR_ROLE)
            .requestMatchers("/admin/superuser/**").hasRole(SUPERUSER_ROLE)
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
