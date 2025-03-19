package ntnu.idi.mushroomidentificationbackend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChangeRequestStatusDTO {
  private String userRequestId;
  private UserRequestStatus newStatus;

}
