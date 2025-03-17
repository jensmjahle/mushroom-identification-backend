package ntnu.idi.mushroomidentificationbackend.mapper;

import java.io.IOException;
import java.util.Objects;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewImageMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewTextMessageDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageType;

@AllArgsConstructor
public class MessageMapper {
  public static NewMessageDTO fromEntityToDto(Message message) throws IOException {
    if (message == null) {
      throw new IllegalArgumentException("Message cannot be null");
    }
    NewMessageDTO messageDTO = null;
    if (message.getMessageType() == MessageType.TEXT) {
      messageDTO = new NewTextMessageDTO(message.getSenderType(), message.getCreatedAt(), message.getContent());
    } else if (message.getMessageType() == MessageType.IMAGE) {
     // messageDTO = new ImageMessageDTO(message.getSenderType(), message.getCreatedAt(), ImageService.loadImageLocally(message.getContent()));
    } else {
      throw new IllegalArgumentException("Message type not supported");
    }
    return messageDTO;
  }
  
  public static Message fromDtoToEntity(NewMessageDTO messageDTO, UserRequest userRequest,
      String content) {
    Message message = new Message();
    message.setSenderType(messageDTO.getSenderType());
    message.setCreatedAt(messageDTO.getCreatedAt());
    message.setUserRequest(userRequest);
    message.setContent(content);
    
    // Set message type
    if (Objects.requireNonNull(messageDTO) instanceof NewTextMessageDTO) {
      message.setMessageType(MessageType.TEXT);
    } else if (messageDTO instanceof NewImageMessageDTO) {
      message.setMessageType(MessageType.IMAGE);
    } else {
      throw new IllegalArgumentException("Message DTO type not supported");
    }
    
    return message;
  }

}
