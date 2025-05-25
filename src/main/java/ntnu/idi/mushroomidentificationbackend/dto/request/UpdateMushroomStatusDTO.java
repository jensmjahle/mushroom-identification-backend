package ntnu.idi.mushroomidentificationbackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ntnu.idi.mushroomidentificationbackend.model.enums.MushroomStatus;

/**
 * Data Transfer Object (DTO) for updating the status of a mushroom.
 * This DTO is used to encapsulate the mushroom ID and the new status
 * for updating the mushroom's status in the system.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class UpdateMushroomStatusDTO {
  private String mushroomId;
  private MushroomStatus status;
  
}
