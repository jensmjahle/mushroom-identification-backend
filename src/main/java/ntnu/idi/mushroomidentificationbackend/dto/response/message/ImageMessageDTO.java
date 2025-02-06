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
public class ImageMessageDTO extends MessageDTO {
  private byte[] image;

  public ImageMessageDTO(MessageSenderType senderType, Date createdAt, byte[] bytes) {
    super(senderType, createdAt);
    this.image = bytes;
  }
}
