package ntnu.idi.mushroomidentificationbackend.controller.auth;

import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.request.UserLoginDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.AuthResponseDTO;
import ntnu.idi.mushroomidentificationbackend.service.AuthenticationService;
import ntnu.idi.mushroomidentificationbackend.dto.request.LoginRequestDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling authentication requests.
 * This controller provides endpoints for admin and user login,
 * including handling anonymous user requests.
 */
@RestController
@RequestMapping("/auth")
public class AuthenticationController {
  private final AuthenticationService authenticationService;
  private final Logger logger = Logger.getLogger(AuthenticationController.class.getName());

  public AuthenticationController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  /**
   * Handles admin login requests.
   * This endpoint authenticates an admin user
   * and returns an authentication token.
   *
   * @param loginRequest the login request containing username and password
   * @return ResponseEntity containing the authentication token
   */
  @PostMapping("/admin/login")
  public ResponseEntity<AuthResponseDTO> adminLogin(@RequestBody LoginRequestDTO loginRequest) {
    logger.info("Received login request for user: " + loginRequest.getUsername());
    String authenticatedToken = authenticationService.authenticate(loginRequest.getUsername(),
        loginRequest.getPassword());
    return ResponseEntity.ok(new AuthResponseDTO(authenticatedToken));
  }

  /**
   * Handles user login requests.
   * This endpoint allows anonymous users to log in
   * using a reference code.
   *
   * @param userLoginDTO the login request containing the reference code
   * @return ResponseEntity containing the authentication token
   */
  @PostMapping("/user/login")
  public ResponseEntity<AuthResponseDTO> userLogin(@RequestBody UserLoginDTO userLoginDTO) {
    logger.info("Received login request for anonymous request" );
    try {

      String authenticatedToken = authenticationService.authenticateUserRequest(
          userLoginDTO.getReferenceCode());
      return ResponseEntity.ok(new AuthResponseDTO(authenticatedToken));

    } catch (Exception ex) {
      // Add delay to slow down brute-force attempts
      try {
        Thread.sleep(3000); // 3 seconds delay
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt(); // Restore interrupt flag
      }
      // For security, avoid leaking which part failed
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }
}
