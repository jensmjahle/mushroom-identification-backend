package ntnu.idi.mushroomidentificationbackend.listener;

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

  @EventListener
  public void handleDisconnect(SessionDisconnectEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = accessor.getSessionId();

    System.out.println(">>> Received SessionDisconnectEvent for session: " + sessionId);

    connectionTracker.getUserRequestId(sessionId).ifPresentOrElse(
        userRequestId -> {
          System.out.println("Releasing lock for user request: " + userRequestId);
          userRequestService.releaseRequestIfLockedByAdmin(userRequestId);
          connectionTracker.removeSession(sessionId);
        },
        () -> {
          System.out.println("No user request bound to session: " + sessionId);
        }
    );
  }
}
