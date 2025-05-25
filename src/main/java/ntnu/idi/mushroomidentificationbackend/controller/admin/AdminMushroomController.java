package ntnu.idi.mushroomidentificationbackend.controller.admin;

import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.request.UpdateMushroomStatusDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MushroomDTO;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketNotificationType;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.MushroomService;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for handling mushroom-related requests in the admin interface.
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/admin/mushrooms")
public class AdminMushroomController {
  private final Logger logger = Logger.getLogger(AdminMushroomController.class.getName());
  private final MushroomService mushroomService;
  private final UserRequestService userRequestService;
  private final JWTUtil jwtUtil;
  private final WebSocketNotificationHandler webSocketNotificationHandler;
  private final SessionRegistry sessionRegistry;

  /**
   * Updates the status of a mushroom in the user's basket.
   * This endpoint allows administrators to change the status of a mushroom
   * identified by the user request ID.
   * 
   * @param userRequestId the ID of the user request to which the mushroom belongs
   * @param token the JWT token for authentication
   * @param updateMushroomStatusDTO the data transfer object containing the new status for the mushroom
   * @return ResponseEntity containing the updated MushroomDTO object
   */
  @PostMapping("/{userRequestId}/status")
  public ResponseEntity<MushroomDTO> updateMushroomStatus(
      @PathVariable String userRequestId,
      @RequestHeader("Authorization") String token, 
      @RequestBody UpdateMushroomStatusDTO updateMushroomStatusDTO) {
    logger.info(() -> String.format("Received request to update mushroom status for user request %s", userRequestId));
    String username = jwtUtil.extractUsername(token.replace("Bearer ", ""));
    userRequestService.tryLockRequest(userRequestId, username);
    MushroomDTO dto = mushroomService.updateMushroomStatus(userRequestId, updateMushroomStatusDTO);
    userRequestService.updateRequest(userRequestId);
    webSocketNotificationHandler.sendRequestUpdateToObservers(userRequestId, WebsocketNotificationType.MUSHROOM_BASKET_UPDATED);
    return ResponseEntity.ok(dto);
  }

}
