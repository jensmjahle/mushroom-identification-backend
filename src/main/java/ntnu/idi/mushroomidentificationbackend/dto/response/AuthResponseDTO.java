package ntnu.idi.mushroomidentificationbackend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Data Transfer Object (DTO) for Authentication Response.
 * This class encapsulates the authentication token returned after a successful login.
 */
@Getter
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
}
