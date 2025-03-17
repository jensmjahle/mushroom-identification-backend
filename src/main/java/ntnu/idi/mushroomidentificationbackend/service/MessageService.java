package ntnu.idi.mushroomidentificationbackend.service;

import java.io.IOException;
import java.util.List;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewImageMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.request.message.NewTextMessageDTO;
import ntnu.idi.mushroomidentificationbackend.mapper.MessageMapper;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageType;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
  private final MessageRepository messageRepository;
  private final UserRequestService userRequestService;
  private final ImageService imageService;

  public MessageService(MessageRepository messageRepository,@Lazy UserRequestService userRequestService,
      ImageService imageService) {
    this.messageRepository = messageRepository;
    this.userRequestService = userRequestService;
    this.imageService = imageService;
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
  
  public Message saveMessage(NewMessageDTO messageDTO, String referenceCode) throws IOException {
    UserRequest userRequest = userRequestService.getUserRequestByReferenceCode(referenceCode);
    String content;
    
    // Save image if a message is an image message
    if(messageDTO instanceof NewImageMessageDTO) {
      content = ImageService.saveImage(((NewImageMessageDTO) messageDTO).getImage(), userRequest.getReferenceCode());
    } else {
      content = ((NewTextMessageDTO) messageDTO).getText();
    }
    Message message = MessageMapper.fromDtoToEntity(messageDTO, userRequest, content);
    return messageRepository.save(message);
  }

  
}
