package ntnu.idi.mushroomidentificationbackend.listener;

import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketConnectionHandler;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketErrorHandler;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
public class WebSocketSubscribeListener {

  private final WebSocketConnectionHandler connectionTracker;
  private final JWTUtil jwtUtil;
  private final UserRequestService userRequestService;
  private final WebSocketErrorHandler webSocketErrorHandler;
  private final Logger logger = Logger.getLogger(WebSocketSubscribeListener.class.getName());

  @EventListener
  public void handleSubscribe(SessionSubscribeEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = accessor.getSessionId();
    String destination = accessor.getDestination();
    String token = accessor.getFirstNativeHeader("Authorization");

    // ✅ Only handle /topic/chatroom/* subscriptions
    if (destination == null || !destination.startsWith("/topic/chatroom/")) {
      return; // ignore /topic/errors or /topic/admins etc.
    }

    if (token == null || token.isEmpty()) {
      logger.severe(String.format("Missing token on SUBSCRIBE (destination: %s, session: %s)", destination, sessionId));
      return;
    }

    String userRequestId = destination.replace("/topic/chatroom/", "");
    token = token.replace("Bearer ", "");

    if (!jwtUtil.isTokenValid(token)) {
      logger.severe(String.format("Invalid token on SUBSCRIBE (session: %s, destination: %s)", sessionId, destination));
      return;
    }

    String username = jwtUtil.extractUsername(token);

    try {
      jwtUtil.validateChatroomToken(token, userRequestId);
      userRequestService.tryLockRequest(userRequestId, username);
      connectionTracker.bindSession(sessionId, userRequestId);
      logger.info(String.format("✅ Bound session %s to userRequest %s", sessionId, userRequestId));
    } catch (Exception e) {
      logger.severe(String.format("❌ Failed to lock request %s for admin %s: %s", userRequestId, username, e.getMessage()));
      webSocketErrorHandler.sendDatabaseError(username, e.getMessage());
    }
  }
}
