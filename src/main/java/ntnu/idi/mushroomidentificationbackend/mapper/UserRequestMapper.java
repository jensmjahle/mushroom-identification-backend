package ntnu.idi.mushroomidentificationbackend.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestWithMessagesDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;

public class UserRequestMapper {

  /**
   * Private constructor to prevent instantiation. Throws UnsupportedOperationException if it's instantiated.
   */
  private UserRequestMapper() {
    throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
  }

  /**
   * Maps a UserRequest entity to a DTO including the request's messages.
   *
   * @param userRequest User requests entity. 
   * @param messages All messages associated with the request.
   * @return An UserRequestWithMessages DTO.
   * @throws IOException If the image-messages are not retrievable from the local storage. 
   */
  public static UserRequestWithMessagesDTO fromEntityToDto(UserRequest userRequest, List<Message> messages)
      throws IOException {
    List<MessageDTO> messageDTOs = new ArrayList<>();
    for (Message message : messages) {
      messageDTOs.add(MessageMapper.fromEntityToDto(message));
    }
    return new UserRequestWithMessagesDTO(
      userRequest.getPasswordHash(),
      userRequest.getCreatedAt(),
      userRequest.getUpdatedAt(),
      userRequest.getStatus(),
      userRequest.getAdmin().getUsername(),
      messageDTOs
    );
  }

  /**
   * Maps a UserRequest entity to a DTO without the request's message.
   *
   * @param userRequest User requests entity.
   * @return |UserRequestWithoutMessagesDTO.
   */
  public static UserRequestDTO fromEntityToDto(UserRequest userRequest) {
    UserRequestDTO userRequestDTO = new UserRequestDTO();
    userRequestDTO.setUserRequestId(userRequest.getUserRequestId());
    userRequestDTO.setCreatedAt(userRequest.getCreatedAt());
    userRequestDTO.setUpdatedAt(userRequest.getUpdatedAt());
    userRequestDTO.setStatus(userRequest.getStatus());
    if (userRequest.getAdmin() != null) {
      userRequestDTO.setUsername(userRequest.getAdmin().getUsername());
    }
    return userRequestDTO;
  }

}
