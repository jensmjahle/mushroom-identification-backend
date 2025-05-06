package ntnu.idi.mushroomidentificationbackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WebSocketNotificationType {
  NEW_CHAT_MESSAGE("You have received a new message.", "notification.request.newChatMessage"),
  MUSHROOM_BASKET_UPDATED("Mushroom basket was updated.", "notification.request.mushroomUpdated"),
  STATUS_CHANGED("The request status has changed.", "notification.request.statusUpdated");

  private final String message;
  private final String i18nKey;
}
