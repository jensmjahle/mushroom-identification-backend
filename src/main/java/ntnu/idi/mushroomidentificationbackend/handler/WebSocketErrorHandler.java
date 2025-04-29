package ntnu.idi.mushroomidentificationbackend.handler;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WebSocketErrorHandler {

  private final SimpMessagingTemplate messagingTemplate;

  public WebSocketErrorHandler(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void sendDatabaseError(String username, String message) {
    System.out.println("[WebSocketErrorHandler] Sending database error to " + username);
    messagingTemplate.convertAndSend("/topic/errors/" + username, Map.of(
        "type", "DATABASE_ERROR",
        "message", message
    ));
  }

  public void sendUnauthorizedError(String username, String message) {
    System.out.println("[WebSocketErrorHandler] Sending unauthorized error to " + username);
    messagingTemplate.convertAndSend("/topic/errors/" + username, Map.of(
        "type", "UNAUTHORIZED",
        "message", message
    ));
  }

  public void sendGeneralError(String username, String message) {
    System.out.println("[WebSocketErrorHandler] Sending general error to " + username);
    messagingTemplate.convertAndSend("/topic/errors/" + username, Map.of(
        "type", "GENERAL_ERROR",
        "message", message
    ));
  }
}
