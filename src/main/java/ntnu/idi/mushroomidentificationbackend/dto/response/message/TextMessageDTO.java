package ntnu.idi.mushroomidentificationbackend.dto.response.message;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.dto.response.message.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TextMessageDTO extends MessageDTO {
  private String text;

  public TextMessageDTO(MessageSenderType senderType, Date createdAt, String content) {
    super(senderType, createdAt);
    this.text = content;
  }
}
