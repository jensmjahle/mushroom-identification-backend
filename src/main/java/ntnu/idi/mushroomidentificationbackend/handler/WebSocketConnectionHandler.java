package ntnu.idi.mushroomidentificationbackend.handler;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class WebSocketConnectionHandler {

  private final Map<String, String> sessionToRequestMap = new ConcurrentHashMap<>(); // sessionId → userRequestId

  public void bindSession(String sessionId, String userRequestId) {
    System.out.println("Binding session " + sessionId + " to request " + userRequestId);
    sessionToRequestMap.put(sessionId, userRequestId);
  }

  public Optional<String> getUserRequestId(String sessionId) {
    return Optional.ofNullable(sessionToRequestMap.get(sessionId));
  }

  public void removeSession(String sessionId) {
    System.out.println("Removing session binding for session: " + sessionId);
    sessionToRequestMap.remove(sessionId);
  }
}
