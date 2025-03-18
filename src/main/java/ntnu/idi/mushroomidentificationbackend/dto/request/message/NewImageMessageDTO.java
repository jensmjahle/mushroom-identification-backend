package ntnu.idi.mushroomidentificationbackend.dto.request.message;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class NewImageMessageDTO extends NewMessageDTO {
  private MultipartFile image;

  public NewImageMessageDTO(MessageSenderType senderType, Date createdAt, MultipartFile image) {
    super(senderType, createdAt);
    this.image = image;
  }
}
