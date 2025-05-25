package ntnu.idi.mushroomidentificationbackend.controller.websocket;

import lombok.RequiredArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for WebSocket-related information endpoints.
 * This controller provides endpoints to retrieve information about WebSocket sessions,
 * specifically the count of online global admins.
 */
@RestController
@RequestMapping("/api/websocket")
@RequiredArgsConstructor
public class WebSocketInfoController {

  private final SessionRegistry sessionRegistry;

  /**
   * Retrieves the count of online global admins.
   * This endpoint is used to check how many global admins are currently online
   * via WebSocket connections.
   *
   * @return the count of online global admins
   */
  @GetMapping("/admins/online-count")
  public long getOnlineAdminCount() {
    return sessionRegistry.countActiveGlobalAdmins();
  }
}
