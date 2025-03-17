package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import ntnu.idi.mushroomidentificationbackend.exception.UserNotFoundException;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;
import ntnu.idi.mushroomidentificationbackend.repository.AdminRepository;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthenticationService {
  private final AdminRepository adminRepository;
  private final JWTUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;

  public AuthenticationService(AdminRepository adminRepository, JWTUtil jwtUtil, PasswordEncoder passwordEncoder) {
    this.adminRepository = adminRepository;
    this.jwtUtil = jwtUtil;
    this.passwordEncoder = passwordEncoder;
  }

  /**
   * Authenticates a user by verifying the provided password against the stored hash.
   *
   * @param username The admin/moderator's username.
   * @param enteredPassword The password entered during login.
   * @return The session token is authentication is successful.
   */
  public String authenticate(String username, String enteredPassword) {
    Optional<Admin> adminOpt = adminRepository.findByUsername(username);

    if (adminOpt.isEmpty()) {
      throw new UserNotFoundException("no such user in database"); // Username not found in the database
    }

    Admin admin = adminOpt.get();
    if (!passwordEncoder.matches(enteredPassword, admin.getPasswordHash())) {
      throw new UnauthorizedAccessException("password is incorrect"); // Invalid password
    }

    return jwtUtil.generateToken(admin.getUsername(), admin.getRole().toString()); // Authentication successful,
    // return token
  }
}
