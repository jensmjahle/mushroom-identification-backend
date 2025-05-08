package ntnu.idi.mushroomidentificationbackend.handler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketRole;
import ntnu.idi.mushroomidentificationbackend.model.websocket.SessionInfo;
import org.springframework.stereotype.Component;

@Component
public class SessionRegistry {

  private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

  public void registerSession(SessionInfo sessionInfo) {
    sessions.put(sessionInfo.getSessionId(), sessionInfo);
  }

  public void unregisterSession(String sessionId) {
    sessions.remove(sessionId);
  }

  public Optional<SessionInfo> getSessionInfo(String sessionId) {
    return Optional.ofNullable(sessions.get(sessionId));
  }

  public List<SessionInfo> getAllSessions() {
    return new ArrayList<>(sessions.values());
  }

  public List<SessionInfo> getSessionsByRole(WebsocketRole role) {
    return sessions.values().stream()
        .filter(session -> session.getRole() == role)
        .collect(Collectors.toList());
  }

  public List<SessionInfo> getSessionsByRequestId(String requestId) {
    return sessions.values().stream()
        .filter(session -> requestId.equals(session.getRequestId()))
        .collect(Collectors.toList());
  }

  public List<SessionInfo> getSessionsByUserId(String userId) {
    return sessions.values().stream()
        .filter(session -> userId.equals(session.getUserId()))
        .collect(Collectors.toList());
  }

  public long countByRole(WebsocketRole role) {
    return sessions.values().stream()
        .filter(session -> session.getRole() == role)
        .count();
  }

  public boolean isUserOnline(String userId) {
    return sessions.values().stream()
        .anyMatch(session -> userId.equals(session.getUserId()));
  }

  public boolean isRequestBeingViewed(String requestId) {
    return sessions.values().stream()
        .anyMatch(session -> requestId.equals(session.getRequestId()));
  }
  
  public void promoteToRequestOwner(String requestId, String userId) {
    sessions.values().stream()
        .filter(session -> session.getRequestId() != null && session.getRequestId().equals(requestId) && session.getUserId().equals(userId))
        .forEach(session -> {
          session.setRole(WebsocketRole.ADMIN_REQUEST_OWNER);
          sessions.put(session.getSessionId(), session);
        });
  }
}
