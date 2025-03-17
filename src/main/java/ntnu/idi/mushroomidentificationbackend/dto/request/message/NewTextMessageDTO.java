package ntnu.idi.mushroomidentificationbackend.dto.request.message;

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
public class NewTextMessageDTO extends NewMessageDTO {
  private String text;

  public NewTextMessageDTO(MessageSenderType senderType, Date createdAt, String content) {
    super(senderType, createdAt);
    this.text = content;
  }
}
