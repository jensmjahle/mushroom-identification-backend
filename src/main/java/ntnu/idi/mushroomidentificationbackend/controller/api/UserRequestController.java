package ntnu.idi.mushroomidentificationbackend.controller.api;


import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewUserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketNotificationType;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling user requests for public users.
 * This controller provides endpoints for creating new user requests
 * and retrieving user request details.
 */
@RestController
@RequestMapping("/api/requests")
public class UserRequestController {
  private final JWTUtil jwtUtil;
  private final UserRequestService userRequestService;
  private final WebSocketNotificationHandler webSocketNotificationHandler;
  private final Logger logger = Logger.getLogger(UserRequestController.class.getName());
  
  @Autowired
  public UserRequestController(JWTUtil jwtUtil, UserRequestService userRequestService,
      WebSocketNotificationHandler webSocketNotificationHandler) {
    this.jwtUtil = jwtUtil;
    this.userRequestService = userRequestService;
    this.webSocketNotificationHandler = webSocketNotificationHandler;
  }

  /**
   * Handles the creation of a new user request.
   * This endpoint processes the new user request
   * and returns a reference code
   * for the request.
   * 
   * @param newUserRequestDTO the data transfer object containing the new user request details
   * @return ResponseEntity containing the reference code for the new user request
   */
  @PostMapping("/create")
  public ResponseEntity<String> createUserRequest(@ModelAttribute NewUserRequestDTO newUserRequestDTO) {
    logger.info("Received new user request");
    String referenceCode = userRequestService.processNewUserRequest(newUserRequestDTO);
    return ResponseEntity.ok(referenceCode);
  }

  /**
   * Retrieves the user request details for the authenticated user.
   * This endpoint extracts the user request ID from the JWT token
   * and returns the corresponding user request details.
   * 
   * @param token the JWT token containing the user request ID
   * @return ResponseEntity containing the user request details
   */
  @GetMapping("/me")
  public ResponseEntity<UserRequestDTO> getRequest(@RequestHeader("Authorization") String token) {
    logger.info("Retrieving user request");
    String userRequestId = jwtUtil.extractUsername(token.replace("Bearer ", ""));
    UserRequestDTO userRequestDTO = userRequestService.getUserRequestDTO(userRequestId);
    webSocketNotificationHandler.sendRequestUpdateToObservers(userRequestId,
        WebsocketNotificationType.USER_LOGGED_IN);
    return ResponseEntity.ok(userRequestDTO);
  }
  
}
