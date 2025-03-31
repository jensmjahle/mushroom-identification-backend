package ntnu.idi.mushroomidentificationbackend.security;


import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.logging.Logger;

@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

  private final JWTUtil jwtUtil;
  private static final Logger logger = Logger.getLogger(WebSocketHandshakeInterceptor.class.getName());

  public WebSocketHandshakeInterceptor(JWTUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
      WebSocketHandler wsHandler, Map<String, Object> attributes) {

    String query = request.getURI().getQuery();
    String clientIP = request.getRemoteAddress() != null ? request.getRemoteAddress().toString() : "Unknown IP";

    if (query == null || !query.contains("token=")) {
      logger.warning("WebSocket handshake rejected: No token provided.");
      return false; // Reject connection
    }

    String token = query.split("token=")[1];

    if (!jwtUtil.isTokenValid(token)) {
      logger.warning("WebSocket handshake rejected: Invalid token.");
      return false; // Reject connection
    }

    return true; // Allow connection
  }

  @Override
  public void afterHandshake(@NotNull ServerHttpRequest request, @NotNull ServerHttpResponse response,
      @NotNull WebSocketHandler wsHandler, @NotNull Exception exception) {
    if (exception != null) {
      logger.warning("WebSocket Handshake Failed: " + exception.getMessage());
    } else {
      logger.info("WebSocket Handshake Successful!");
    }
  }
}
