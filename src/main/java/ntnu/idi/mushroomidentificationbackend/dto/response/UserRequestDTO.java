package ntnu.idi.mushroomidentificationbackend.dto.response;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import org.springframework.lang.Nullable;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UserRequestDTO {
  private String userRequestId;
  private Date createdAt;
  private Date updatedAt;
  private UserRequestStatus status;
  @Nullable
  private String username;
}
