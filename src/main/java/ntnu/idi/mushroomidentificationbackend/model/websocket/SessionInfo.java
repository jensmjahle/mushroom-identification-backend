package ntnu.idi.mushroomidentificationbackend.model.websocket;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketRole;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class SessionInfo {
  private String sessionId;
  private String userId;
  private WebsocketRole role;
  private String requestId;

  public boolean isGlobalSession() {
    return requestId == null || requestId.isBlank();
  }

}
