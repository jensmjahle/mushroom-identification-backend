package ntnu.idi.mushroomidentificationbackend.mapper;

import java.util.Objects;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewImageMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewTextMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageType;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import org.springframework.stereotype.Component;



public class MessageMapper {

 private MessageMapper() {
    throw new IllegalStateException("Utility class");
  }

  

  /**
   * Convert a Message entity to a MessageDTO.
   */
  public static MessageDTO fromEntityToDto(Message message) {
    if (message == null) {
      throw new IllegalArgumentException("Message cannot be null");
    }
    return new MessageDTO(
        message.getMessageId(),
        message.getSenderType(),
        message.getMessageType(),
        message.getContent(), 
        message.getCreatedAt()
    );
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
