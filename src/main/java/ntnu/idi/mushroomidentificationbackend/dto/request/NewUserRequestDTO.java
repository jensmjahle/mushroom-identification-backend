package ntnu.idi.mushroomidentificationbackend.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
  @NotBlank(message = "text must not be blank")
  private String text;

  @NotEmpty(message = "at least one mushroom must be provided")
  @Valid
  private List<NewMushroomDTO> mushrooms;
}
