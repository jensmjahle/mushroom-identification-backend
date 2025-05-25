package ntnu.idi.mushroomidentificationbackend.model.enums;

/**
 * Enum representing the roles of users in the websocket context.
 * This is used to determine the permissions and capabilities
 * of users in the WebSocket communication.
 */
public enum WebsocketRole {
  ANONYMOUS_USER,
  ADMIN_REQUEST_OWNER,
  ADMIN_REQUEST_OBSERVER,
  ADMIN_REQUEST_CHATTER,
  ADMIN_GLOBAL_OWNER,
  ADMIN_GLOBAL_OBSERVER,
  ADMIN_PERSONAL_OBSERVER,
}
