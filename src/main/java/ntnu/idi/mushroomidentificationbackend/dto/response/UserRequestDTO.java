package ntnu.idi.mushroomidentificationbackend.dto.response;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.BasketBadgeType;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;
import org.springframework.lang.Nullable;

/**
 * Data Transfer Object (DTO) for User Request.
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class UserRequestDTO {
  private String userRequestId;
  private Date createdAt;
  private Date updatedAt;
  private UserRequestStatus status;
  private long numberOfMushrooms;
  @Nullable
  private String username;
  private List<BasketBadgeType> basketSummaryBadges;


}
