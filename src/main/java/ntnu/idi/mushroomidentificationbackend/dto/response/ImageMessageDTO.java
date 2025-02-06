package ntnu.idi.mushroomidentificationbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ImageMessageDTO extends  MessageDTO {
  private byte[] image;
}
