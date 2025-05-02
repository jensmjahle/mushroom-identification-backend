package ntnu.idi.mushroomidentificationbackend.listener;

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

  @EventListener
  public void handleSubscribe(SessionSubscribeEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = accessor.getSessionId();
    String destination = accessor.getDestination();
    String token = accessor.getFirstNativeHeader("Authorization");

    System.out.println(">>> SUBSCRIBE event fired");
    System.out.println("Session ID: " + sessionId);
    System.out.println("Destination: " + destination);
    System.out.println("Token: " + token);

    if (destination != null && destination.startsWith("/topic/chatroom/") && token != null) {
      String userRequestId = destination.replace("/topic/chatroom/", "");
      token = token.replace("Bearer ", "");

      if (jwtUtil.isTokenValid(token)) {
        jwtUtil.validateChatroomToken(token, userRequestId);
        connectionTracker.bindSession(sessionId, userRequestId);
        System.out.println("✅ Bound session " + sessionId + " to userRequest " + userRequestId);
      } else {
        System.out.println("❌ Invalid token during SUBSCRIBE");
      }
    } else {
      System.out.println("❌ Invalid subscribe destination or missing token");
    }
  }
}
