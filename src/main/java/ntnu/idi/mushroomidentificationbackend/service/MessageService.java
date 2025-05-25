package ntnu.idi.mushroomidentificationbackend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.mapper.MessageMapper;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Service class for handling message-related operations.
 */
@Service
public class MessageService {
  private final MessageRepository messageRepository;
  private final UserRequestService userRequestService;
  private final Logger logger = Logger.getLogger(MessageService.class.getName());

  public MessageService(MessageRepository messageRepository,@Lazy UserRequestService userRequestService) {
    this.messageRepository = messageRepository;
    this.userRequestService = userRequestService;
  }

  /**
   * Get all messages associated with a user request, ordered by creation date.
   *
   * @param userRequest The user request for which messages are to be retrieved.
   * @return A list of messages associated with the user request, ordered by creation date.
   */
  public List<Message> getAllMessagesToUserRequest(UserRequest userRequest) {
    return messageRepository.findByUserRequestOrderByCreatedAtAsc(userRequest);
  }

  /**
   * Save a new message to the database.
   * This method checks if the user request is not completed before saving the message.
   * If the user request is completed,
   * it throws a DatabaseOperationException.
   *
   * @param messageDTO The DTO containing the message details to be saved.
   * @param userRequestId The ID of the user request to which the message belongs.
   * @return A MessageDTO containing the saved message details.
   */
  public MessageDTO saveMessage(NewMessageDTO messageDTO, String userRequestId) {
    UserRequest userRequest = userRequestService.getUserRequest(userRequestId);
    
    if (userRequest.getStatus() == UserRequestStatus.COMPLETED) {
      throw new DatabaseOperationException("Cannot add message to a completed user request");
    }
    
    Message message = MessageMapper.fromDtoToEntity(messageDTO, userRequest);
    Message savedMessage = messageRepository.save(message);
    return MessageMapper.fromEntityToDto(savedMessage);
  }

  /**
   * Get a message by its ID.
   * This method retrieves a message from the database using its ID.
   *
   * @param messageId The ID of the message to be retrieved.
   * @return A MessageDTO containing the message details.
   */
  public MessageDTO getMessageDTO(String messageId) {
    Message message = messageRepository.findById(messageId).orElseThrow();
    return MessageMapper.fromEntityToDto(message);
  }
  
  /**
   * Get chat history for a user request.
   *
   * @param userRequestId The user request ID.
   * @return A list of messages as DTOs.
   */
  public List<MessageDTO> getChatHistory(String userRequestId) {
    try {
    UserRequest userRequest = userRequestService.getUserRequest(userRequestId);
    List<Message> messages = getAllMessagesToUserRequest(userRequest);
    List<MessageDTO> messageDTOs = new ArrayList<>();
      for (Message message : messages) {
        messageDTOs.add(MessageMapper.fromEntityToDto(message));
      }
    return messageDTOs;
    } catch (Exception e) {
      throw new DatabaseOperationException("Failed to retrieve chat history");
    }
  }
  
}
