package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.exception.RequestNotFoundException;
import ntnu.idi.mushroomidentificationbackend.exception.UnauthorizedAccessException;
import ntnu.idi.mushroomidentificationbackend.model.entity.Admin;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.repository.AdminRepository;
import ntnu.idi.mushroomidentificationbackend.repository.UserRequestRepository;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

/**
 * Service class for handling authentication operations.
 * This class provides methods to authenticate admin users and user requests.
 */
@Service
public class AuthenticationService {

  /**
   * A precomputed BCrypt hash with no known matching plaintext, used to perform a dummy
   * password comparison when the submitted username does not exist. This keeps the response
   * time for "unknown username" and "wrong password" cases roughly equal, closing the timing
   * side-channel that would otherwise let an attacker enumerate valid admin usernames.
   */
  private static final String DUMMY_PASSWORD_HASH =
      "$2a$10$7EqJtq98hPqEX7fNZaFWoOhi5L5DlvmXKfVUP8sxvSbaXTKZ8SWpi";
  private static final String INVALID_CREDENTIALS_MESSAGE = "Invalid username or password";

  private final AdminRepository adminRepository;
  private final JWTUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;
  private final UserRequestRepository userRequestRepository;
  private final UserRequestService userRequestService;


  public AuthenticationService(AdminRepository adminRepository, JWTUtil jwtUtil, PasswordEncoder passwordEncoder,
      UserRequestRepository userRequestRepository, UserRequestService userRequestService) {
    this.adminRepository = adminRepository;
    this.jwtUtil = jwtUtil;
    this.passwordEncoder = passwordEncoder;
    this.userRequestRepository = userRequestRepository;
    this.userRequestService = userRequestService;
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
      // Perform a dummy hash comparison so the response takes about as long as a real
      // password check, and throw the same exception/message as a wrong password would.
      passwordEncoder.matches(enteredPassword, DUMMY_PASSWORD_HASH);
      throw new UnauthorizedAccessException(INVALID_CREDENTIALS_MESSAGE);
    }

    Admin admin = adminOpt.get();
    if (!passwordEncoder.matches(enteredPassword, admin.getPasswordHash())) {
      throw new UnauthorizedAccessException(INVALID_CREDENTIALS_MESSAGE);
    }

    return jwtUtil.generateToken(admin.getUsername(), admin.getRole().toString()); // Authentication successful,
    // return token
  }

  /**
   * Authenticates a user request by verifying the provided reference code against the stored hash.
   *
   * @param referenceCode The reference code provided by the user.
   * @return The session token if authentication is successful.
   */
  public String authenticateUserRequest(String referenceCode) {
    
    Optional<UserRequest> userRequestOpt = userRequestRepository.findByLookUpKey(userRequestService.hashReferenceCodeForLookup(referenceCode));
    
    if (userRequestOpt.isEmpty()) {
      throw new RequestNotFoundException("no such request in database");
    }
    if (!passwordEncoder.matches(referenceCode, userRequestOpt.get().getPasswordHash())) {
      throw new RequestNotFoundException("no such request in database");
    }
    return jwtUtil.generateToken(userRequestOpt.get().getUserRequestId(), "USER");
  }
}
