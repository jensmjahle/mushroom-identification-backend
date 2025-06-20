package ntnu.idi.mushroomidentificationbackend.listener;

import java.util.Optional;
import java.util.logging.Logger;

import lombok.RequiredArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketNotificationType;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketRole;
import ntnu.idi.mushroomidentificationbackend.model.websocket.SessionInfo;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 *  Listener for WebSocket disconnect events.
 *  Handles user disconnections from WebSocket sessions,
 *  releasing any associated user requests
 *  and notifying observers of the disconnection.
 */
@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener {

  private final SessionRegistry sessionRegistry;
  private final UserRequestService userRequestService;
  private final WebSocketNotificationHandler webSocketNotificationHandler;
  private final Logger logger = Logger.getLogger(WebSocketDisconnectListener.class.getName());

  /**
   * Handles WebSocket disconnect events.
   * Processes the disconnection by unregistering the session,
   * releasing any locked user requests,
   * and notifying observers of the disconnection.
   *
   * @param event the SessionDisconnectEvent containing session details
   */
  @EventListener
  public void handleDisconnect(SessionDisconnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = accessor.getSessionId();

    Optional<SessionInfo> sessionOpt = sessionRegistry.getSessionInfo(sessionId);

    if (sessionOpt.isPresent()) {
      SessionInfo info = sessionOpt.get();
      LogHelper.info(logger, "Session disconnected: {0} (user: {1}, roles: {2}, request: {3})",
          sessionId, info.getUserId(), info.getRoles(), info.getRequestId());

      if (info.getRequestId() != null) {
        userRequestService.releaseRequestIfLockedByAdmin(info.getRequestId(), info.getUserId());

        if (info.hasRole(WebsocketRole.ANONYMOUS_USER)) {
          webSocketNotificationHandler.sendRequestUpdateToObservers(
              info.getRequestId(), WebsocketNotificationType.USER_LOGGED_OUT);
        }

        if (info.hasRole(WebsocketRole.ADMIN_REQUEST_OWNER)) {
          webSocketNotificationHandler.sendRequestUpdateToObservers(
              info.getRequestId(), WebsocketNotificationType.ADMIN_LEFT_REQUEST);
        }
      }

      sessionRegistry.unregisterSession(sessionId);
    } else {
      LogHelper.warning(logger, "Session ID not found during disconnect: {0}", sessionId);
    }
  }
}
