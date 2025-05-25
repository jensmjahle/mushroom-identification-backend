package ntnu.idi.mushroomidentificationbackend.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) for creating a new user request.
 * This DTO is used to encapsulate the text description
 * and a list of mushrooms associated with the new user request.
 * It is typically used when a user submits a request
 * to identify mushrooms or provide information about them.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NewUserRequestDTO {
  private String text;
  private List<NewMushroomDTO> mushrooms;
}
