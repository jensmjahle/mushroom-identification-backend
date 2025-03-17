package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.exception.RequestNotFoundException;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import ntnu.idi.mushroomidentificationbackend.exception.UserNotFoundException;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.repository.AdminRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class AuthenticationService {
  private final AdminRepository adminRepository;
  private final JWTUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;
  private final UserRequestRepository userRequestRepository;


  public AuthenticationService(AdminRepository adminRepository, JWTUtil jwtUtil, PasswordEncoder passwordEncoder,
      UserRequestRepository userRequestRepository) {
    this.adminRepository = adminRepository;
    this.jwtUtil = jwtUtil;
    this.passwordEncoder = passwordEncoder;
    this.userRequestRepository = userRequestRepository;
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

  public String authenticateUserRequest(String referenceCode) {
    
    Optional<UserRequest> userRequestOpt = userRequestRepository.findByPasswordHash(UserRequestService.hashReferenceCode(referenceCode));
    
    if (userRequestOpt.isEmpty()) {
      throw new RequestNotFoundException("no such request in database");
    }
    
    return jwtUtil.generateToken(referenceCode, "USER");
  }
}
