package ntnu.idi.mushroomidentificationbackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UpdateMushroomStatusDTO {
  private String mushroomId;
  private String status;
  
}
