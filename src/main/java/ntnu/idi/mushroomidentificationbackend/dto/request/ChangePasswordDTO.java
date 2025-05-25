package ntnu.idi.mushroomidentificationbackend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Data Transfer Object (DTO) for changing an admin user's password.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ChangePasswordDTO {
  @NotBlank(message = "Old password cannot be blank")
  private String oldPassword; 
  @NotBlank(message = "New password cannot be blank")
  private String newPassword;
  @NotBlank(message = "Confirm password cannot be blank")
  private String confirmPassword;

}
