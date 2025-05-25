package ntnu.idi.mushroomidentificationbackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for Login Request.
 * This class is used to encapsulate the login credentials
 * for admin user authentication.
 */
@AllArgsConstructor
@Getter
@Setter
public class LoginRequestDTO {
  private String username;
  private String password;

}
