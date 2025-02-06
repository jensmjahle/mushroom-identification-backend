package ntnu.idi.mushroomidentificationbackend.mapper;

import java.io.IOException;
import lombok.AllArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.dto.response.message.ImageMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.message.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.message.TextMessageDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageType;
import ntnu.idi.mushroomidentificationbackend.service.ImageService;

@AllArgsConstructor
public class MessageMapper {
  public static MessageDTO fromEntityToDto(Message message) throws IOException {
    if (message == null) {
      throw new IllegalArgumentException("Message cannot be null");
    }
    MessageDTO messageDTO;
    if (message.getMessageType() == MessageType.TEXT) {
      messageDTO = new TextMessageDTO(message.getSenderType(), message.getCreatedAt(), message.getContent());
    } else if (message.getMessageType() == MessageType.IMAGE) {
      messageDTO = new ImageMessageDTO(message.getSenderType(), message.getCreatedAt(), ImageService.loadImageLocally(message.getContent()));
    } else {
      throw new IllegalArgumentException("Message type not supported");
    }
    return messageDTO;
  }

}
