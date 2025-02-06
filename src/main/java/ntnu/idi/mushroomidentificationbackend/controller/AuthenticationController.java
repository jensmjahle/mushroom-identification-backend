package ntnu.idi.mushroomidentificationbackend.controller;

import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.response.AuthResponseDTO;
import ntnu.idi.mushroomidentificationbackend.service.AuthenticationService;
import ntnu.idi.mushroomidentificationbackend.dto.request.LoginRequestDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthenticationController {
  private final AuthenticationService authenticationService;
  private final Logger logger = Logger.getLogger(AuthenticationController.class.getName());

  public AuthenticationController(AuthenticationService authenticationService) {
    this.authenticationService = authenticationService;
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponseDTO> login(@RequestBody LoginRequestDTO loginRequest) {
    logger.info("Received login request for user: " + loginRequest.getUsername());
    String authenticatedToken = authenticationService.authenticate(loginRequest.getUsername(), loginRequest.getPassword());
    
    if (authenticatedToken != null) {
      return ResponseEntity.ok(new AuthResponseDTO(authenticatedToken));
    } else {
      return ResponseEntity.status(401).body(null);
    }
  }
}
