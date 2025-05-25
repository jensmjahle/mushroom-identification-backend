package ntnu.idi.mushroomidentificationbackend.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enum representing different types of notifications
 * that can be sent over WebSocket.
 */
@AllArgsConstructor
@Getter
public enum WebsocketNotificationType {
  NEW_CHAT_MESSAGE("You have received a new message.", "notification.request.newChatMessage"),
  MUSHROOM_BASKET_UPDATED("Mushroom basket was updated.", "notification.request.mushroomUpdated"),
  STATUS_CHANGED("The request status has changed.", "notification.request.statusUpdated"),
  REQUEST_CURRENTLY_UNDER_REVIEW("The request is currently under review.", "notification.request.currentlyUnderReview"),
  USER_LOGGED_IN("User logged in.", "notification.user.loggedIn"),
  USER_LOGGED_OUT("User logged out.", "notification.user.loggedOut"),
  ADMIN_LEFT_REQUEST("Admin left the request.", "notification.request.adminLeft");
  
  private final String message;
  private final String i18nKey;
}
