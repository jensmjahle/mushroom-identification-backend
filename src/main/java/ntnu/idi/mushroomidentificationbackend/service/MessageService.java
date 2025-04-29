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

@Service
public class MessageService {
  private final MessageRepository messageRepository;
  private final UserRequestService userRequestService;
  private final Logger logger = Logger.getLogger(MessageService.class.getName());

  public MessageService(MessageRepository messageRepository,@Lazy UserRequestService userRequestService) {
    this.messageRepository = messageRepository;
    this.userRequestService = userRequestService;
  }
  public List<Message> getAllMessagesToUserRequest(UserRequest userRequest) {
    return messageRepository.findByUserRequestOrderByCreatedAtAsc(userRequest);
  }
  
  public MessageDTO saveMessage(NewMessageDTO messageDTO, String userRequestId) {
    UserRequest userRequest = userRequestService.getUserRequest(userRequestId);
    
    if (userRequest.getStatus() == UserRequestStatus.COMPLETED) {
      throw new DatabaseOperationException("Cannot add message to a completed user request");
    }
    
    Message message = MessageMapper.fromDtoToEntity(messageDTO, userRequest);
    Message savedMessage = messageRepository.save(message);
    return MessageMapper.fromEntityToDto(savedMessage);
  }
  
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
