package ntnu.idi.mushroomidentificationbackend.listener;

import java.util.logging.Logger;
import lombok.RequiredArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketConnectionHandler;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
public class WebSocketSubscribeListener {

  private final WebSocketConnectionHandler connectionTracker;
  private final JWTUtil jwtUtil;
  private final Logger logger = Logger.getLogger(WebSocketSubscribeListener.class.getName());

  @EventListener
  public void handleSubscribe(SessionSubscribeEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = accessor.getSessionId();
    String destination = accessor.getDestination();
    String token = accessor.getFirstNativeHeader("Authorization");
    

    if (destination != null && destination.startsWith("/topic/chatroom/") && token != null) {
      String userRequestId = destination.replace("/topic/chatroom/", "");
      token = token.replace("Bearer ", "");

      if (jwtUtil.isTokenValid(token)) {
        jwtUtil.validateChatroomToken(token, userRequestId);
        connectionTracker.bindSession(sessionId, userRequestId);
      } else {
        logger.severe("Invalid token during SUBSCRIBE: " + token);
      }
    } else {
      logger.severe("Invalid subscribe destination or missing token: " + destination);
    }
  }
}
