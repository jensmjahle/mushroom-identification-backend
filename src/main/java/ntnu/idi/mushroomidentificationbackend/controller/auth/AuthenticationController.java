package ntnu.idi.mushroomidentificationbackend.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.request.UserLoginDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.AuthResponseDTO;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.security.LoginAttemptService;
import ntnu.idi.mushroomidentificationbackend.security.TokenBlocklistService;
import ntnu.idi.mushroomidentificationbackend.service.AuthenticationService;
import ntnu.idi.mushroomidentificationbackend.dto.request.LoginRequestDTO;
import org.springframework.http.HttpHeaders;
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
  private final LoginAttemptService loginAttemptService;
  private final JWTUtil jwtUtil;
  private final TokenBlocklistService tokenBlocklistService;
  private static final String BEARER = "Bearer ";
  private final Logger logger = Logger.getLogger(AuthenticationController.class.getName());

  public AuthenticationController(AuthenticationService authenticationService,
      LoginAttemptService loginAttemptService, JWTUtil jwtUtil,
      TokenBlocklistService tokenBlocklistService) {
    this.authenticationService = authenticationService;
    this.loginAttemptService = loginAttemptService;
    this.jwtUtil = jwtUtil;
    this.tokenBlocklistService = tokenBlocklistService;
  }

  /**
   * Handles admin login requests.
   * This endpoint authenticates an admin user
   * and returns an authentication token.
   * Repeated failed attempts from the same client for the same username
   * are temporarily locked out to defend against brute-force attacks.
   *
   * @param loginRequest the login request containing username and password
   * @param request the HTTP request, used to identify the calling client
   * @return ResponseEntity containing the authentication token
   */
  @PostMapping("/admin/login")
  public ResponseEntity<AuthResponseDTO> adminLogin(@RequestBody LoginRequestDTO loginRequest,
      HttpServletRequest request) {
    logger.info("Received login request for user: " + loginRequest.getUsername());
    String attemptKey = request.getRemoteAddr() + ":" + loginRequest.getUsername();
    loginAttemptService.checkAllowed(attemptKey);
    try {
      String authenticatedToken = authenticationService.authenticate(loginRequest.getUsername(),
          loginRequest.getPassword());
      loginAttemptService.recordSuccess(attemptKey);
      return ResponseEntity.ok(new AuthResponseDTO(authenticatedToken));
    } catch (RuntimeException e) {
      loginAttemptService.recordFailure(attemptKey);
      throw e;
    }
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

  /**
   * Logs out the caller by revoking the JWT used to authenticate the request, so it can no
   * longer be used to access protected endpoints even though it has not yet naturally expired.
   *
   * @param authHeader the Authorization header containing the JWT to revoke
   * @return an empty 200 OK response
   */
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(@RequestHeader(HttpHeaders.AUTHORIZATION) String authHeader) {
    String token = authHeader.replace(BEARER, "").trim();
    String tokenId = jwtUtil.extractTokenId(token);
    Date expiration = jwtUtil.extractExpiration(token);
    tokenBlocklistService.revoke(tokenId, expiration);
    logger.info("Token revoked on logout");
    return ResponseEntity.ok().build();
  }
}
