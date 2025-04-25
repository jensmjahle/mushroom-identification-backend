package ntnu.idi.mushroomidentificationbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class UpdateProfileDTO {
  @NotBlank(message = "Username cannot be blank")
  private String firstname;
  @NotBlank(message = "Lastname cannot be blank")
  private String lastname;
  @NotBlank(message = "Email cannot be blank")
  private String email;

}
