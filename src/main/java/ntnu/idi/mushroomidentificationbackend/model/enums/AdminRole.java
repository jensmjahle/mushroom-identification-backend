package ntnu.idi.mushroomidentificationbackend.model.enums;

/**
 * Enum representing the roles of administrators in the system.
 * This is used to determine the permissions and capabilities
 * of administrators in the application.
 */
public enum AdminRole {
  SUPERUSER, // Has all permissions, can create other admins
  MODERATOR,  // Can moderate user requests, but cannot create other admins
}
