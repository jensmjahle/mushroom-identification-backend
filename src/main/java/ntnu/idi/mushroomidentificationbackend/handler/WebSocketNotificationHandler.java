package ntnu.idi.mushroomidentificationbackend.handler;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class WebSocketNotificationHandler {

  private final SimpMessagingTemplate messagingTemplate;
  private static final String NOTIFICATION_TOPIC = "/topic/notifications/";

  public WebSocketNotificationHandler(SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void sendInfo(String username, String message, String i18nKey) {
    messagingTemplate.convertAndSend(NOTIFICATION_TOPIC + username, Map.of(
        "type", "INFO",
        "message", message,
        "i18n", i18nKey
    ));
  }

  public void sendAlert(String username, String message, String i18nKey) {
    messagingTemplate.convertAndSend(NOTIFICATION_TOPIC + username, Map.of(
        "type", "ALERT",
        "message", message,
        "i18n", i18nKey
    ));
  }

  public void sendCustom(String username, String type, String message, String i18nKey) {
    messagingTemplate.convertAndSend(NOTIFICATION_TOPIC + username, Map.of(
        "type", type,
        "message", message,
        "i18n", i18nKey
    ));
  }
}
