package ntnu.idi.mushroomidentificationbackend.model.enums;

/**
 * Enum representing different types of badges that can be assigned to a basket
 * based on the mushrooms it contains.
 */
public enum BasketBadgeType {
  TOXIC_MUSHROOM_PRESENT,
  PSYCHOACTIVE_MUSHROOM_PRESENT,
  UNIDENTIFIABLE_MUSHROOM_PRESENT,
  NON_PSILOCYBIN_MUSHROOM_PRESENT,
  UNKNOWN_MUSHROOM_PRESENT,
  BAD_PICTURES_MUSHROOM_PRESENT,

  ALL_MUSHROOMS_PROCESSED,
  NO_MUSHROOMS_PROCESSED,

  ALL_MUSHROOMS_ARE_TOXIC,
  ALL_MUSHROOMS_ARE_PSILOCYBIN,
  ALL_MUSHROOMS_ARE_NON_PSILOCYBIN,
  ALL_MUSHROOMS_ARE_UNKNOWN,
  ALL_MUSHROOMS_ARE_UNIDENTIFIABLE,
  ALL_MUSHROOMS_ARE_BAD_PICTURES
}
