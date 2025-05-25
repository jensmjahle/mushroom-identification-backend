package ntnu.idi.mushroomidentificationbackend.handler;

import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketNotificationType;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Handler for WebSocket notifications.
 * This component is responsible for sending
 * notifications to users via WebSocket.
 * It supports different types of notifications,
 * including info, alert, and custom messages.
 */
@Component
public class WebSocketNotificationHandler {

  private final SimpMessagingTemplate messagingTemplate;
  private static final String NOTIFICATION_TOPIC = "/topic/notifications/";

  public WebSocketNotificationHandler(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  /**
   * Sends an informational message to a user via WebSocket.
   *
   * @param username the username of the recipient
   * @param message the message to send
   * @param i18nKey the internationalization key for the message
   */
  public void sendInfo(String username, String message, String i18nKey) {
    messagingTemplate.convertAndSend(NOTIFICATION_TOPIC + username, Map.of(
        "type", "INFO",
        "message", message,
        "i18n", i18nKey
    ));
  }

  /**
   * Sends an alert message to a user via WebSocket.
   * This method is used to notify users of important events or issues.
   * 
   * @param username the username of the recipient
   * @param message the alert message to send
   * @param i18nKey the internationalization key for the alert message
   */
  public void sendAlert(String username, String message, String i18nKey) {
    messagingTemplate.convertAndSend(NOTIFICATION_TOPIC + username, Map.of(
        "type", "ALERT",
        "message", message,
        "i18n", i18nKey
    ));
  }

  /**
   * Sends a custom notification to a user via WebSocket.
   * This method allows for sending notifications
   * with a specific type and message,
   * including an internationalization key.
   *
   * @param username the username of the recipient
   * @param type the type of notification (e.g., "INFO", "ALERT", etc.)
   * @param message the message to send
   * @param i18nKey the internationalization key for the message
   */
  public void sendCustom(String username, String type, String message, String i18nKey) {
    messagingTemplate.convertAndSend(NOTIFICATION_TOPIC + username, Map.of(
        "type", type,
        "message", message,
        "i18n", i18nKey
    ));
  }

  /**
   * Sends a request update notification to observers.
   * This method is used to notify all observers
   * of a specific request about updates
   * related to that request.
   *
   * @param requestId the ID of the request being updated
   * @param type the type of update (e.g., "USER_LOGGED_OUT", "ADMIN_LEFT_REQUEST", etc.)
   */
  public void sendRequestUpdateToObservers(String requestId, WebsocketNotificationType type) {
    String topic = "/topic/request/" + requestId;
    messagingTemplate.convertAndSend(topic, Map.of(
        "type", type.name(),
        "message", type.getMessage(),
        "i18n", type.getI18nKey()
    ));
  }

}
