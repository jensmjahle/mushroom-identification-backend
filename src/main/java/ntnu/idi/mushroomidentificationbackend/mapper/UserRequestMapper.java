package ntnu.idi.mushroomidentificationbackend.mapper;

import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.BasketBadgeType;

/**
 * Utility class for mapping UserRequest entities to UserRequestDTOs.
 */
public class UserRequestMapper {

  /**
   * Private constructor to prevent instantiation. Throws UnsupportedOperationException if it's instantiated.
   */
  private UserRequestMapper() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Maps a UserRequest entity to a DTO without the request's message.
   *
   * @param userRequest User requests entity.
   * @return |UserRequestWithoutMessagesDTO.
   */
  public static UserRequestDTO fromEntityToDto(UserRequest userRequest, List<BasketBadgeType> badges, long mushroomCount) {
    UserRequestDTO dto = new UserRequestDTO();
    dto.setUserRequestId(userRequest.getUserRequestId());
    dto.setCreatedAt(userRequest.getCreatedAt());
    dto.setUpdatedAt(userRequest.getUpdatedAt());
    dto.setStatus(userRequest.getStatus());
    dto.setBasketSummaryBadges(badges);
    dto.setNumberOfMushrooms(mushroomCount);
    if (userRequest.getAdmin() != null) {
      dto.setUsername(userRequest.getAdmin().getUsername());
    }
    return dto;
  }


}
