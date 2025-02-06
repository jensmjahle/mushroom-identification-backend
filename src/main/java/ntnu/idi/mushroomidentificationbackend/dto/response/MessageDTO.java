package ntnu.idi.mushroomidentificationbackend.dto.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public abstract class MessageDTO 
{
  private MessageSenderType senderType;
  private Date createdAt;
}
