package ntnu.idi.mushroomidentificationbackend.dto.response.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.dto.response.message.MessageDTO;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageMessageDTO extends MessageDTO {
  private byte[] image;
}
