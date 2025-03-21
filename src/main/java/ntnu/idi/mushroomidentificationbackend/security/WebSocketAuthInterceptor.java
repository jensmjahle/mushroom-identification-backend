package ntnu.idi.mushroomidentificationbackend.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

  private final JWTUtil jwtUtil;
  private static final Logger logger = Logger.getLogger(WebSocketAuthInterceptor.class.getName());

  public WebSocketAuthInterceptor(JWTUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  /*
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
    
    String token = accessor.getFirstNativeHeader("Authorization");
 
    if (token.isEmpty()) {
      logger.warning("WebSocket connection rejected: Missing token.");
      throw new IllegalArgumentException("Unauthorized WebSocket connection.");
    }

    token = token.replace("Bearer ", "");
    

    if (!jwtUtil.isTokenValid(token)) {
      logger.warning("WebSocket connection rejected: Invalid token.");
      throw new IllegalArgumentException("Unauthorized WebSocket connection.");
    }

    return message; // Allow connection if token is valid
  }
  
   */
  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.SEND.equals(accessor.getCommand())) {
      String token = accessor.getFirstNativeHeader("Authorization");

      if (token == null || token.isEmpty()) {
        logger.warning("WebSocket SEND rejected: Missing token.");
        throw new IllegalArgumentException("Unauthorized WebSocket connection.");
      }

      token = token.replace("Bearer ", "");

      if (!jwtUtil.isTokenValid(token)) {
        logger.warning("WebSocket SEND rejected: Invalid token.");
        throw new IllegalArgumentException("Unauthorized WebSocket connection.");
      }
    }

    return message;
  }


}
