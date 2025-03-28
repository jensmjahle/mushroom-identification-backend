package ntnu.idi.mushroomidentificationbackend.dto.response;

import java.util.Date;
import java.util.List;
import ntnu.idi.mushroomidentificationbackend.model.enums.MushroomStatus;

public class MushroomDTO {

  private long mushroomId;
  private Date createdAt;
  private Date updatedAt;
  private MushroomStatus mushroomStatus;
  private List<String> imageUrls;


}
