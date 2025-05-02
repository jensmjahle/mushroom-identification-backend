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

  @NotNull
  @Override
  public Message<?> preSend(@NotNull Message<?> message, @NotNull MessageChannel channel) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

    if (StompCommand.CONNECT.equals(accessor.getCommand())) {
      String token = accessor.getFirstNativeHeader("Authorization");

      if (token != null && !token.isEmpty()) {
        token = token.replace("Bearer ", "");

        if (jwtUtil.isTokenValid(token)) {
          String username = jwtUtil.extractUsername(token);
          accessor.setUser(new StompPrincipal(username));
          logger.info("WebSocket connected user: " + username);
        }
      } else if (accessor.getUser() == null) {
        Object username = accessor.getSessionAttributes().get("username");
        if (username != null) {
          accessor.setUser(new StompPrincipal((String) username));
          logger.info("WebSocket connected user: " + username);

        } else {
          logger.warning("WebSocket message received without authenticated user!");
        }
      }
    }

    return message;
  }

  }
