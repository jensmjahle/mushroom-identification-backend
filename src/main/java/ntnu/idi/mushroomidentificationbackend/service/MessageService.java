package ntnu.idi.mushroomidentificationbackend.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewImageMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewTextMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.exception.DatabaseOperationException;
import ntnu.idi.mushroomidentificationbackend.mapper.MessageMapper;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageType;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
  private final MessageRepository messageRepository;
  private final UserRequestService userRequestService;
  private final ImageService imageService;
  private final JWTUtil jwtUtil;

  public MessageService(MessageRepository messageRepository,@Lazy UserRequestService userRequestService,
      ImageService imageService, JWTUtil jwtUtil) {
    this.messageRepository = messageRepository;
    this.userRequestService = userRequestService;
    this.imageService = imageService;
    this.jwtUtil = jwtUtil;
  }
  public List<Message> getAllMessagesToUserRequest(UserRequest userRequest) {
    return messageRepository.findByUserRequestOrderByCreatedAtDesc(userRequest);
  }
  public List<Message> getAllTextMessagesToUserRequest(UserRequest userRequest) {
    return messageRepository.findByUserRequestAndMessageTypeOrderByCreatedAtDesc(userRequest, MessageType.TEXT);
  }
  public List<Message> getAllImageMessagesToUserRequest(UserRequest userRequest) {
    return messageRepository.findByUserRequestAndMessageTypeOrderByCreatedAtDesc(userRequest, MessageType.IMAGE);
  }
  
  public MessageDTO saveMessage(NewMessageDTO messageDTO, String userRequestId) throws IOException {
    UserRequest userRequest = userRequestService.getUserRequest(userRequestId);
    String content;
    
    // Save image if a message is an image message
    if(messageDTO instanceof NewImageMessageDTO) {
      content = ImageService.saveImage(((NewImageMessageDTO) messageDTO).getImage(), userRequest.getPasswordHash());
    } else {
      content = ((NewTextMessageDTO) messageDTO).getText();
    }
    Message message = MessageMapper.fromDtoToEntity(messageDTO, userRequest, content);
    Message savedMessage = messageRepository.save(message);
    return MessageMapper.fromEntityToDto(savedMessage);
  }
  
  public MessageDTO getMessageDTO(String messageId) {
    Message message = messageRepository.findById(messageId).orElseThrow();
    if (message.getMessageType().equals(MessageType.IMAGE)) {
      message.setContent(jwtUtil.generateSignedImageUrl(messageId, message.getContent()));
    }
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
        if (message.getMessageType().equals(MessageType.IMAGE)) {
          message.setContent(jwtUtil.generateSignedImageUrl(message.getMessageId(), message.getContent()));
        }
        messageDTOs.add(MessageMapper.fromEntityToDto(message));
      }
    return messageDTOs;
    } catch (Exception e) {
      throw new DatabaseOperationException("Failed to retrieve chat history");
    }
  }
}
