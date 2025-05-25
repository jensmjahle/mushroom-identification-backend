package ntnu.idi.mushroomidentificationbackend.handler;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Handler for WebSocket error notifications.
 * This component is responsible for sending error messages
 * to users via WebSocket.
 */
@Component
public class WebSocketErrorHandler {

  private final SimpMessagingTemplate messagingTemplate;
  private static final String ERROR_TOPIC = "/topic/errors/";

  public WebSocketErrorHandler(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Sends an error message to a user via WebSocket.
   * 
   * @param username the username of the recipient
   * @param type the type of error (e.g., DATABASE_ERROR, REQUEST_LOCKED, UNAUTHORIZED, GENERAL_ERROR)
   * @param message the error message to send
   * @param i18nKey the internationalization key for the error message
   */
  private void sendError(String username, String type, String message, String i18nKey) {
    Map<String, String> payload = new LinkedHashMap<>();
    payload.put("type", type);
    payload.put("message", message);
    if (i18nKey != null && !i18nKey.isBlank()) {
      payload.put("i18n", i18nKey);
    }

    messagingTemplate.convertAndSend(ERROR_TOPIC + username, payload);
  }

  /**
   * Sends a database error message to a user via WebSocket.
   * 
   * @param username the username of the recipient
   * @param message the error message to send
   */
  public void sendDatabaseError(String username, String message) {
    sendError(username, "DATABASE_ERROR", message, "errors.DATABASE_ERROR");
  }

  /**
   * Sends a request locked error message to a user via WebSocket.
   * 
   * @param username the username of the recipient
   * @param message the error message to send
   */
  public void sendRequestLockedError(String username, String message) {
    sendError(username, "REQUEST_LOCKED", message, "errors.REQUEST_LOCKED");
  }

  /**
   *  Sends an unauthorized error message to a user via WebSocket.
   *  
   * @param username the username of the recipient
   * @param message the error message to send
   */
  public void sendUnauthorizedError(String username, String message) {
    sendError(username, "UNAUTHORIZED", message, "errors.UNAUTHORIZED");
  }

  /**
   * Sends a general error message to a user via WebSocket.
   * 
   * @param username the username of the recipient
   * @param message the error message to send
   */
  public void sendGeneralError(String username, String message) {
    sendError(username, "GENERAL_ERROR", message, "errors.GENERAL_ERROR");
  }
}
