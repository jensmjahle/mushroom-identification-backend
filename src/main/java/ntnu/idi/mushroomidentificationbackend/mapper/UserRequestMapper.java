package ntnu.idi.mushroomidentificationbackend.mapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewUserRequestDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.RetrieveRequestAnswerDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.message.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;

public class UserRequestMapper {
  public static RetrieveRequestAnswerDTO fromEntityToDto(UserRequest userRequest, List<Message> messages)
      throws IOException {
    List<MessageDTO> messageDTOs = new ArrayList<>();
    for (Message message : messages) {
      messageDTOs.add(MessageMapper.fromEntityToDto(message));
    }
    return new RetrieveRequestAnswerDTO(
      userRequest.getReferenceCode(),
      userRequest.getCreatedAt(),
      userRequest.getUpdatedAt(),
      userRequest.getStatus(),
      messageDTOs
    );
  }

}
