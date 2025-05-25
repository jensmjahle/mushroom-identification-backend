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

  /**
   * Adds a role to the session.
   * 
   * @param role the role to add
   */
  public void addRole(WebsocketRole role) {
    this.roles.add(role);
  }

  /**
   * Checks if the session has a specific role.
   *
   * @param role the role to check
   * @return true if the session has the role, false otherwise
   */
  public boolean hasRole(WebsocketRole role) {
    return this.roles.contains(role);
  }
}
