package ntnu.idi.mushroomidentificationbackend.listener;

import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketConnectionHandler;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketErrorHandler;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
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
  private final WebSocketNotificationHandler webSocketNotificationHandler;
  private static final Logger logger = Logger.getLogger(WebSocketSubscribeListener.class.getName());

  @EventListener
  public void handleSubscribe(SessionSubscribeEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = accessor.getSessionId();
    String destination = accessor.getDestination();
    String token = accessor.getFirstNativeHeader("Authorization");

    // Only handle /topic/chatroom/* subscriptions
    if (destination == null || !destination.startsWith("/topic/chatroom/")) {
      return; // ignore /topic/errors or /topic/admins etc.
    }

    if (token == null || token.isEmpty()) {
      LogHelper.warning(logger, "Missing token on SUBSCRIBE (destination: {0}, session: {1})", destination, sessionId);
      return;
    }

    String userRequestId = destination.replace("/topic/chatroom/", "");
    token = token.replace("Bearer ", "");

    if (!jwtUtil.isTokenValid(token)) {
      LogHelper.warning(logger, "Invalid token on SUBSCRIBE (destination: {0}, session: {1})", destination, sessionId);
      return;
    }

    String username = jwtUtil.extractUsername(token);

    try {
      jwtUtil.validateChatroomToken(token, userRequestId);
      userRequestService.tryLockRequest(userRequestId, username);
      connectionTracker.bindSession(sessionId, userRequestId);
      LogHelper.info(logger, "User {0} subscribed to chatroom {1}", username, userRequestId);
    } catch (Exception e) {
      LogHelper.severe(logger, "Failed to lock request {0} for admin {1}: {2}", userRequestId, username, e.getMessage());
      webSocketNotificationHandler.sendInfo(username, "Obs! This request is currently being handled by another administrator", "notification.request.locked");
    }
  }
}
