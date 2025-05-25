package ntnu.idi.mushroomidentificationbackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import ntnu.idi.mushroomidentificationbackend.model.enums.AdminRole;

/**
 * Data Transfer Object (DTO) for creating an Admin.
 * This class is used to transfer data from the client to the server when creating a new admin user.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CreateAdminDTO {

  @NotBlank(message = "Username is required")
  @Size(min = 4, max = 20, message = "Username must be between 4 and 20 characters")
  private String username;

  @NotBlank(message = "Password is required")
  @Size(min = 8, message = "Password must be at least 8 characters long")
  private String password;

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;
  
  @NotBlank(message = "Admin role is required")
  private AdminRole role;

}
