package ntnu.idi.mushroomidentificationbackend.handler;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WebSocketErrorHandler {

  private final SimpMessagingTemplate messagingTemplate;
  private static final String ERROR_TOPIC = "/topic/errors/";

  public WebSocketErrorHandler(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void sendDatabaseError(String username, String message) {
    messagingTemplate.convertAndSend(ERROR_TOPIC + username, Map.of(
        "type", "DATABASE_ERROR",
        "message", message
    ));
  }

  public void sendUnauthorizedError(String username, String message) {
    messagingTemplate.convertAndSend(ERROR_TOPIC + username, Map.of(
        "type", "UNAUTHORIZED",
        "message", message
    ));
  }

  public void sendGeneralError(String username, String message) {
    messagingTemplate.convertAndSend(ERROR_TOPIC + username, Map.of(
        "type", "GENERAL_ERROR",
        "message", message
    ));
  }
}
