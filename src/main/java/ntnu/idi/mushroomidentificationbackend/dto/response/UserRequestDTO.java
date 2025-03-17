package ntnu.idi.mushroomidentificationbackend.dto.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;

@Getter
@AllArgsConstructor
@Setter
public class UserRequestDTO {
  private String userRequestId;
  private Date createdAt;
  private Date updatedAt;
  private UserRequestStatus status;
  private String username;
}
