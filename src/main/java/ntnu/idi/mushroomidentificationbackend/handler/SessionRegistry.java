package ntnu.idi.mushroomidentificationbackend.handler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.ToString;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketRole;
import ntnu.idi.mushroomidentificationbackend.model.websocket.SessionInfo;
import org.springframework.stereotype.Component;

@Component
@ToString
public class SessionRegistry {

  private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

  public void registerSession(SessionInfo sessionInfo) {
    sessions.merge(sessionInfo.getSessionId(), sessionInfo, (existing, incoming) -> {
      for (WebsocketRole role : incoming.getRoles()) {
        existing.addRole(role);
      }
      return existing;
    });
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
        .filter(session -> session.hasRole(role))
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
        .filter(session -> session.hasRole(role))
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
        .filter(session ->
            requestId.equals(session.getRequestId()) &&
                userId.equals(session.getUserId()) &&
                session.hasRole(WebsocketRole.ADMIN_REQUEST_OBSERVER)
        )
        .forEach(session -> session.addRole(WebsocketRole.ADMIN_REQUEST_OWNER));
  }

  public long countActiveGlobalAdmins() {
    return sessions.values().stream()
        .filter(session -> session.hasRole(WebsocketRole.ADMIN_GLOBAL_OBSERVER))
        .map(SessionInfo::getUserId)
        .filter(Objects::nonNull)
        .distinct()
        .count();
  }
}
