package ntnu.idi.mushroomidentificationbackend.dto.request;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;

/**
 * Data Transfer Object (DTO) for creating a new message.
 * This DTO is used to encapsulate the sender type,
 * the creation date, and the content of the message.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewMessageDTO 
{
  private MessageSenderType senderType;
  private Date createdAt;
  private String content;
}
