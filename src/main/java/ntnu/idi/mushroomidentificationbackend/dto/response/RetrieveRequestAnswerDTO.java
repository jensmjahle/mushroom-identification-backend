package ntnu.idi.mushroomidentificationbackend.dto.response;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ntnu.idi.mushroomidentificationbackend.model.enums.UserRequestStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RetrieveRequestAnswerDTO {
  private String referenceCode;
  private Date createdAt;
  private Date updatedAt;
  private UserRequestStatus status;
  private List<MessageDTO> messages;
  

}
