package ntnu.idi.mushroomidentificationbackend.service;

import ntnu.idi.mushroomidentificationbackend.model.entity.Message;
import org.springframework.stereotype.Service;

@Service
public class MessageService {
  
  public Message createMessage() {
    return new Message(text);
  }

}
