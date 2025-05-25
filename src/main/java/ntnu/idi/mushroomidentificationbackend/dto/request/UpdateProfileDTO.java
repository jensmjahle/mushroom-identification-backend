package ntnu.idi.mushroomidentificationbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) for updating a user's profile.
 * This DTO is used to encapsulate the user's first name, last name, and email
 * for updating the user's profile information in the system.
 */
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
