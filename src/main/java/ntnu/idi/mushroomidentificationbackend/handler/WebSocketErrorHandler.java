package ntnu.idi.mushroomidentificationbackend.handler;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WebSocketErrorHandler {

  private final SimpMessagingTemplate messagingTemplate;
  private static final String ERROR_TOPIC = "/topic/errors/";

  public WebSocketErrorHandler(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  private void sendError(String username, String type, String message, String i18nKey) {
    Map<String, String> payload = new LinkedHashMap<>();
    payload.put("type", type);
    payload.put("message", message);
    if (i18nKey != null && !i18nKey.isBlank()) {
      payload.put("i18n", i18nKey);
    }

    messagingTemplate.convertAndSend(ERROR_TOPIC + username, payload);
  }

  public void sendDatabaseError(String username, String message) {
    sendError(username, "DATABASE_ERROR", message, "errors.DATABASE_ERROR");
  }

  public void sendRequestLockedError(String username, String message) {
    sendError(username, "REQUEST_LOCKED", message, "errors.REQUEST_LOCKED");
  }

  public void sendUnauthorizedError(String username, String message) {
    sendError(username, "UNAUTHORIZED", message, "errors.UNAUTHORIZED");
  }

  public void sendGeneralError(String username, String message) {
    sendError(username, "GENERAL_ERROR", message, "errors.GENERAL_ERROR");
  }
}
