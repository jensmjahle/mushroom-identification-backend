package ntnu.idi.mushroomidentificationbackend.security;

import java.util.logging.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private final JWTUtil jwtUtil;
  private static final Logger logger = Logger.getLogger(WebSocketAuthInterceptor.class.getName());

  public WebSocketAuthInterceptor(JWTUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    StompCommand command = accessor.getCommand();

    if (command == null) return message;

    String token = accessor.getFirstNativeHeader("Authorization");

    if (token != null && token.startsWith("Bearer ")) {
      token = token.replace("Bearer ", "");
    }

    // Allow if authenticated and valid token
    if (!jwtUtil.isTokenValid(token)) {
      logger.warning("WebSocket rejected: Invalid or missing token");
      return null; // Block the message
    }

    String username = jwtUtil.extractUsername(token);
    String role = jwtUtil.extractRole(token);
    accessor.setUser(new StompPrincipal(username));

    switch (command) {
      case CONNECT -> logger.info("WebSocket CONNECT: " + username);

      case SUBSCRIBE -> {
        String destination = accessor.getDestination();
        if (destination == null) return null;

        if (destination.startsWith("/topic/errors/")) {
          String targetUser = destination.replace("/topic/errors/", "");
          if (!username.equals(targetUser)) {
            logger.warning("Unauthorized error topic access by " + username);
            return null;
          }
        }

        if (destination.startsWith("/topic/admins")) {
          if (!role.equals("SUPERUSER") && !role.equals("MODERATOR")) {
            logger.warning("Non-admin tried to access /topic/admins");
            return null;
          }
        }

        // Chatroom access is validated later in listener (admins or request owner)
      }

      case SEND -> {
        String destination = accessor.getDestination();
        if (destination == null) return null;

        if (destination.startsWith("/app/chat/")) {
          String userRequestId = destination.replace("/app/chat/", "");

          // Let the controller validate ownership/admin status securely
          try {
            jwtUtil.validateChatroomToken(token, userRequestId);
          } catch (Exception e) {
            logger.warning("Blocked unauthorized chat SEND to " + userRequestId + " by " + username);
            return null;
          }
        }
      }
    }

    return message;
  }
}

