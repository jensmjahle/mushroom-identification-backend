package ntnu.idi.mushroomidentificationbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ntnu.idi.mushroomidentificationbackend.model.enums.AdminRole;

/**
 * Data Transfer Object (DTO) for Admin.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class AdminDTO {
  private String username;
  private String firstname;
  private String lastname;
  private String email;
  private AdminRole role;
  

}
