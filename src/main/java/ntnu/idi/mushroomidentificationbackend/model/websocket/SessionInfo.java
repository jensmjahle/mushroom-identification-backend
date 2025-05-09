package ntnu.idi.mushroomidentificationbackend.model.websocket;

import java.util.HashSet;
import java.util.Set;
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
  private Set<WebsocketRole> roles = new HashSet<>();
  private String requestId;

  public void addRole(WebsocketRole role) {
    this.roles.add(role);
  }

  public boolean hasRole(WebsocketRole role) {
    return this.roles.contains(role);
  }

  public boolean isGlobalSession() {
    return requestId == null || requestId.isBlank();
  }
}
