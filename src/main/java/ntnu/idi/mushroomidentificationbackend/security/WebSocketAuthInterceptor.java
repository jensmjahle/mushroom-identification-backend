package ntnu.idi.mushroomidentificationbackend.security;

import static ntnu.idi.mushroomidentificationbackend.util.LogHelper.info;
import static ntnu.idi.mushroomidentificationbackend.util.LogHelper.warning;

import java.util.logging.Logger;

import ntnu.idi.mushroomidentificationbackend.model.websocket.StompPrincipal;
import org.jetbrains.annotations.NotNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * Interceptor for WebSocket messages to handle authentication and authorization.
 * This interceptor checks the validity of JWT tokens,
 * validates user access to specific topics,
 * and sets the user principal for the session.
 */
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private final JWTUtil jwtUtil;
  private static final Logger logger = Logger.getLogger(WebSocketAuthInterceptor.class.getName());

  public WebSocketAuthInterceptor(JWTUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  /**
   * Intercepts WebSocket messages before they are sent to the channel.
   * This method checks the command type,
   * validates the JWT token,
   * and performs authorization checks based on the command and destination.
   * If the token is invalid or the user does not have access to the requested topic,
   * the message is rejected (returns null).
   *
   * @param message the WebSocket message to be intercepted
   * @param channel the channel to which the message is being sent
   * @return the original message if valid, or null if the message is rejected
   */
  @Override
  public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    StompCommand command = accessor.getCommand();

    if (command == null || command == StompCommand.DISCONNECT) {
      return message;
    }

    String token = accessor.getFirstNativeHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
      token = token.replace("Bearer ", "");
    }
    if (!jwtUtil.isTokenValid(token)) {
      warning(logger, "WebSocket rejected: Invalid or missing token");
      return null;
    }

    String username = jwtUtil.extractUsername(token);
    String role = jwtUtil.extractRole(token);
    accessor.setUser(new StompPrincipal(username));

    switch (command) {
      case CONNECT -> info(logger, "WebSocket CONNECT: {0}", username);

      case SUBSCRIBE -> {
        String destination = accessor.getDestination();
        if (destination == null) return null;

        if (destination.startsWith("/topic/errors/")) {
          String targetUser = destination.replace("/topic/errors/", "");
          if (!username.equals(targetUser)) {
            warning(logger, "Unauthorized error topic access by {0}", username);
            return null;
          }
        }

        if (destination.startsWith("/topic/notifications/")) {
          String targetUser = destination.replace("/topic/notifications/", "");
          if (!username.equals(targetUser)) {
            warning(logger, "Unauthorized notification topic access by {0}", username);
            return null;
          }
        }

        if (destination.startsWith("/topic/admins")) {
          if (!role.equals("SUPERUSER") && !role.equals("MODERATOR")) {
            warning(logger, "Non-admin tried to access /topic/admins: {0}", username);
            return null;
          }
        }

        if (destination.startsWith("/topic/chatroom/")) {
          String requestId = destination.replace("/topic/chatroom/", "");
          try {
            jwtUtil.validateChatroomToken(token, requestId);
          } catch (Exception e) {
            warning(logger, "Unauthorized chatroom access attempt by {0} for {1}", username, requestId);
            return null;
          }
        }

        if (destination.startsWith("/topic/request/")) {
          String requestId = destination.replace("/topic/request/", "");
          try {
            jwtUtil.validateChatroomToken(token, requestId);
          } catch (Exception e) {
            warning(logger, "Unauthorized request-notification access attempt by {0} for {1}", username, requestId);
            return null;
          }
        }
      }

      case SEND -> {
        String destination = accessor.getDestination();
        if (destination == null) return null;

        if (destination.startsWith("/app/chat/")) {
          String userRequestId = destination.replace("/app/chat/", "");
          try {
            jwtUtil.validateChatroomToken(token, userRequestId);
          } catch (Exception e) {
            warning(logger, "Blocked unauthorized chat SEND to {0} by {1}", userRequestId, username);
            return null;
          }
        }
      }
      default -> warning(logger, "Unhandled WebSocket command: {0}", command);
    }

    return message;
  }
}
