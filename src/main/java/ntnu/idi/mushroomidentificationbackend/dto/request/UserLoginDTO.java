package ntnu.idi.mushroomidentificationbackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * Data Transfer Object (DTO) for a public user login.
 */
@AllArgsConstructor
@Getter
@Setter
public class UserLoginDTO {
String referenceCode;
}
