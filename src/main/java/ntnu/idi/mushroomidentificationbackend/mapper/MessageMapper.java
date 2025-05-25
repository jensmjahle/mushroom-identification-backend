package ntnu.idi.mushroomidentificationbackend.mapper;

import ntnu.idi.mushroomidentificationbackend.dto.request.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;

/**
 * Utility class for mapping Message entities to MessageDTOs.
 * This class provides methods to convert
 * Message entities to DTOs and vice versa,
 * ensuring that the data
 * is properly transformed
 * for use in the response layer.
 */
public class MessageMapper {

 private MessageMapper() {
    throw new IllegalStateException("Utility class");
  }


  /**
   * Converts a Message entity to a MessageDTO.
   * This method is used to transform the Message entity
   * into a DTO that can be used in the response layer,
   * avoiding direct exposure of the entity.
   *
   * @param message Message entity to be converted to DTO.
   * @return MessageDTO containing the message's details.
   */
  public static MessageDTO fromEntityToDto(Message message) {
    if (message == null) {
      throw new IllegalArgumentException("Message cannot be null");
    }
    return new MessageDTO(
        message.getMessageId(),
        message.getSenderType(),
        message.getContent(), 
        message.getCreatedAt()
    );
  }

  /**
   * Converts a NewMessageDTO to a Message entity.
   * This method is used to create a new Message entity
   * from the data provided in a NewMessageDTO,
   * which typically comes from a user request.
   *
   * @param messageDTO the DTO containing the message details
   * @param userRequest the UserRequest entity associated with the message
   * @return Message entity populated with data from the DTO
   */
  public static Message fromDtoToEntity(NewMessageDTO messageDTO, UserRequest userRequest) {
    Message message = new Message();
    message.setSenderType(messageDTO.getSenderType());
    message.setCreatedAt(messageDTO.getCreatedAt());
    message.setUserRequest(userRequest);
    message.setContent(messageDTO.getContent());
    
    return message;
  }

}
