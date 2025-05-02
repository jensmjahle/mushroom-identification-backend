package ntnu.idi.mushroomidentificationbackend.listener;

import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketConnectionHandler;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener {

  private final WebSocketConnectionHandler connectionTracker;
  private final UserRequestService userRequestService;
  private final Logger logger = Logger.getLogger(WebSocketDisconnectListener.class.getName());

  @EventListener
  public void handleDisconnect(SessionDisconnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = accessor.getSessionId();
    
    connectionTracker.getUserRequestId(sessionId).ifPresentOrElse(
        userRequestId -> {
          userRequestService.releaseRequestIfLockedByAdmin(userRequestId);
          connectionTracker.removeSession(sessionId);
        },
        () -> {
          logger.severe("Session ID not found in connection tracker: " + sessionId);
          
        }
    );
  }
}
