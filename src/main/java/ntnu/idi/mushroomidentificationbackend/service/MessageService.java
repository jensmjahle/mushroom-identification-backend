package ntnu.idi.mushroomidentificationbackend.service;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import ntnu.idi.mushroomidentificationbackend.model.entity.UserRequest;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageType;
import ntnu.idi.mushroomidentificationbackend.repository.MessageRepository;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
  private final MessageRepository messageRepository;

  public MessageService(MessageRepository messageRepository) {
    this.messageRepository = messageRepository;
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
  
  public Message saveMessage(Message message) {
    return messageRepository.save(message);
  }

}
