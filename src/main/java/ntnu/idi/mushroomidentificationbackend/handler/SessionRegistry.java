package ntnu.idi.mushroomidentificationbackend.handler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import lombok.ToString;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketRole;
import ntnu.idi.mushroomidentificationbackend.model.websocket.SessionInfo;
import org.springframework.stereotype.Component;

/**
 * Registry for managing WebSocket sessions.
 * This component is responsible for
 * registering, unregistering,
 * and retrieving session information.
 * It maintains a map of session IDs to SessionInfo objects,
 * allowing for efficient access
 * and management of active WebSocket sessions.
 */
@Component
@ToString
public class SessionRegistry {

  private final Map<String, SessionInfo> sessions = new ConcurrentHashMap<>();

  /**
   * Registers a new WebSocket session.
   * This method adds a new session to the registry,
   * merging roles if the session already exists.
   *
   * @param sessionInfo the SessionInfo object containing session details
   */
  public void registerSession(SessionInfo sessionInfo) {
    sessions.merge(sessionInfo.getSessionId(), sessionInfo, (existing, incoming) -> {
      for (WebsocketRole role : incoming.getRoles()) {
        existing.addRole(role);
      }
      return existing;
    });
  }

  /**
   * Unregisters a WebSocket session.
   * This method removes a session from the registry
   * based on the provided session ID.
   *
   * @param sessionId the ID of the session to unregister
   */
  public void unregisterSession(String sessionId) {
    sessions.remove(sessionId);
  }

  /**
   * Retrieves session information by session ID.
   *
   * @param sessionId the ID of the session to retrieve
   * @return an Optional containing the SessionInfo if found,
   */
  public Optional<SessionInfo> getSessionInfo(String sessionId) {
    return Optional.ofNullable(sessions.get(sessionId));
  }

  /**
   * Retrieves all registered WebSocket sessions.
   * This method returns a list of all active sessions
   * currently registered in the registry.
   *
   * @return a list of SessionInfo objects representing all sessions
   */
  public List<SessionInfo> getAllSessions() {
    return new ArrayList<>(sessions.values());
  }

  /**
   * Retrieves sessions by a specific role.
   * This method filters the active sessions
   * based on the provided WebsocketRole
   * and returns a list of SessionInfo objects
   * that match the role.
   *
   * @param role the WebsocketRole to filter sessions by
   * @return a list of SessionInfo objects
   */
  public List<SessionInfo> getSessionsByRole(WebsocketRole role) {
    return sessions.values().stream()
        .filter(session -> session.hasRole(role))
        .collect(Collectors.toList());
  }

  /**
   * Retrieves sessions by request ID.
   * This method filters the active sessions
   * based on the provided request ID
   * and returns a list of SessionInfo objects
   * that match the request ID.
   *
   * @param requestId the ID of the request to filter sessions by
   * @return a list of SessionInfo objects
   */
  public List<SessionInfo> getSessionsByRequestId(String requestId) {
    return sessions.values().stream()
        .filter(session -> requestId.equals(session.getRequestId()))
        .collect(Collectors.toList());
  }

  /**
   * Retrieves sessions by user ID.
   * This method filters the active sessions
   * based on the provided user ID
   * and returns a list of SessionInfo objects
   * that match the user ID.
   *
   * @param userId the ID of the user to filter sessions by
   * @return a list of SessionInfo objects
   */
  public List<SessionInfo> getSessionsByUserId(String userId) {
    return sessions.values().stream()
        .filter(session -> userId.equals(session.getUserId()))
        .collect(Collectors.toList());
  }

  /**
   * Counts the number of sessions with a specific role.
   * This method iterates through all active sessions
   * and counts how many have the specified WebsocketRole.
   *
   * @param role the WebsocketRole to count sessions by
   * @return the number of sessions with the specified role
   */
  public long countByRole(WebsocketRole role) {
    return sessions.values().stream()
        .filter(session -> session.hasRole(role))
        .count();
  }

  /**
   * Checks if a user is currently online.
   *
   * @param userId the ID of the user to check
   * @return true if the user is online, false otherwise
   */
  public boolean isUserOnline(String userId) {
    return sessions.values().stream()
        .anyMatch(session -> userId.equals(session.getUserId()));
  }

  /**
   * Checks if a request is currently being viewed.
   * 
   * @param requestId the ID of the request to check
   * @return true if the request is being viewed, false otherwise
   */
  public boolean isRequestBeingViewed(String requestId) {
    return sessions.values().stream()
        .anyMatch(session -> requestId.equals(session.getRequestId()));
  }

  /**
   * Promotes a user to the owner of a request.
   * This method adds the ADMIN_REQUEST_OWNER role
   * to a session if the user
   * is already an ADMIN_REQUEST_OBSERVER
   * for the specified request.
   *
   * @param requestId the ID of the request to promote the user for
   * @param userId the ID of the user to promote
   */
  public void promoteToRequestOwner(String requestId, String userId) {
    sessions.values().stream()
        .filter(session ->
            requestId.equals(session.getRequestId()) &&
                userId.equals(session.getUserId()) &&
                session.hasRole(WebsocketRole.ADMIN_REQUEST_OBSERVER)
        )
        .forEach(session -> session.addRole(WebsocketRole.ADMIN_REQUEST_OWNER));
  }

  /**
   * Counts the number of active global administrators.
   * This method iterates through all active sessions
   * and counts how many have the ADMIN_GLOBAL_OBSERVER role.
   *
   * @return the number of active global administrators
   */
  public long countActiveGlobalAdmins() {
    return sessions.values().stream()
        .filter(session -> session.hasRole(WebsocketRole.ADMIN_GLOBAL_OBSERVER))
        .map(SessionInfo::getUserId)
        .filter(Objects::nonNull)
        .distinct()
        .count();
  }
}
