package ntnu.idi.mushroomidentificationbackend.model.enums;

public enum AdminRole {
  SUPERUSER, // Has all permissions, can create other admins
  MODERATOR,  // Can moderate user requests, but cannot create other admins
}
