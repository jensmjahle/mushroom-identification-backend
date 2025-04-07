package ntnu.idi.mushroomidentificationbackend.dto.response;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ntnu.idi.mushroomidentificationbackend.model.enums.MushroomStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class MushroomDTO {

  private String mushroomId;
  private Date createdAt;
  private Date updatedAt;
  private MushroomStatus mushroomStatus;
  private List<String> imageUrls;


}
