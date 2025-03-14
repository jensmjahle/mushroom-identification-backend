package ntnu.idi.mushroomidentificationbackend.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestWithMessagesDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.UserRequestWithoutMessagesDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.message.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;

public class UserRequestMapper {
  public static UserRequestWithMessagesDTO fromEntityToDto(UserRequest userRequest, List<Message> messages)
      throws IOException {
    List<MessageDTO> messageDTOs = new ArrayList<>();
    for (Message message : messages) {
      messageDTOs.add(MessageMapper.fromEntityToDto(message));
    }
    return new UserRequestWithMessagesDTO(
      userRequest.getReferenceCode(),
      userRequest.getCreatedAt(),
      userRequest.getUpdatedAt(),
      userRequest.getStatus(),
      userRequest.getAdmin().getUsername(),
      messageDTOs
    );
  }
  
  public static UserRequestWithoutMessagesDTO fromEntityToDto(UserRequest userRequest) {
    return new UserRequestWithoutMessagesDTO(
        userRequest.getReferenceCode(),
        userRequest.getCreatedAt(),
        userRequest.getUpdatedAt(),
        userRequest.getStatus(),
        userRequest.getAdmin().getUsername()
    );
  }

}
