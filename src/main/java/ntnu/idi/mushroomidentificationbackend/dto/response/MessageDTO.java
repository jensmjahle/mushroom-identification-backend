package ntnu.idi.mushroomidentificationbackend.dto.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;
@AllArgsConstructor
@Getter
@Setter
public class MessageDTO {
  private String messageId;
  private MessageSenderType senderType;
  private String content;
  private Date createdAt;
}
