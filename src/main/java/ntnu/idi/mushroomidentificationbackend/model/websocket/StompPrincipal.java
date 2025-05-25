package ntnu.idi.mushroomidentificationbackend.model.websocket;

import java.security.Principal;

/**
 * Represents a STOMP principal with a name.
 * This class implements the Principal interface
 * to provide a way to identify the user
 * in STOMP-based WebSocket communication.
 */
public class StompPrincipal implements Principal {

  private final String name;

  public StompPrincipal(String name) {
    this.name = name;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof StompPrincipal)) return false;
    return name.equals(((StompPrincipal) o).name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return name;
  }
}
