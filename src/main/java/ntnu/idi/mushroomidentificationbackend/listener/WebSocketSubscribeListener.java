package ntnu.idi.mushroomidentificationbackend.listener;

import java.util.Collections;
import java.util.HashSet;
import java.util.logging.Logger;

import lombok.RequiredArgsConstructor;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.model.enums.AdminRole;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketNotificationType;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketRole;
import ntnu.idi.mushroomidentificationbackend.model.websocket.SessionInfo;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import ntnu.idi.mushroomidentificationbackend.util.LogHelper;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

/**
 * Listener for WebSocket subscription events.
 * Handles user subscriptions to various WebSocket topics,
 * including chatrooms, admin notifications,
 * error streams, and request notifications.
 */
@Component
@RequiredArgsConstructor
public class WebSocketSubscribeListener {

  private final JWTUtil jwtUtil;
  private final UserRequestService userRequestService;
  private final WebSocketNotificationHandler webSocketNotificationHandler;
  private final SessionRegistry sessionRegistry;
  private static final Logger logger = Logger.getLogger(WebSocketSubscribeListener.class.getName());

  /**
   * Handles WebSocket subscription events.
   * Processes the subscription based on the destination
   * and token provided in the event.
   * @param event the SessionSubscribeEvent containing subscription details
   */
  @EventListener
  public void handleSubscribe(SessionSubscribeEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = accessor.getSessionId();
    String destination = accessor.getDestination();
    String token = accessor.getFirstNativeHeader("Authorization");

    if (destination == null) return;

    if (token == null || token.isEmpty()) {
      LogHelper.warning(logger, "Missing token on SUBSCRIBE (destination: {0}, session: {1})", destination, sessionId);
      return;
    }

    token = token.replace("Bearer ", "");

    if (!jwtUtil.isTokenValid(token)) {
      LogHelper.warning(logger, "Invalid token on SUBSCRIBE (destination: {0}, session: {1})", destination, sessionId);
      return;
    }

    String username = jwtUtil.extractUsername(token);

    try {
      if (destination.startsWith("/topic/chatroom/")) {
        handleChatroomSubscription(destination, token, sessionId, username);
      } else if (destination.equals("/topic/admins")) {
        sessionRegistry.registerSession(
            new SessionInfo(sessionId, username, new HashSet<>(Collections.singleton(WebsocketRole.ADMIN_GLOBAL_OBSERVER)), null)
        );
        LogHelper.info(logger, "Admin {0} subscribed to the global admin channel", username);
      } else if (destination.equals("/topic/notifications/" + username)) {
        handleAdminPersonalNotificationSubscription(sessionId, username, token);
      } else if (destination.equals("/topic/errors/" + username)) {
        handleErrorStreamSubscription(sessionId, username, token);
      } else if (destination.startsWith("/topic/request/")) {
        handleRequestNotificationSubscription(destination, token, sessionId);
      } else {
        LogHelper.warning(logger, "Unhandled subscription destination for user {0}: {1}", username, destination);
      }
    } catch (Exception e) {
      LogHelper.severe(logger, "Exception during subscription handling for session {0}: {1}", sessionId, e.getMessage());
    }
  }

  /**
   * Handles subscription to a chatroom.
   * Validates the JWT token,
   * locks the request if the user is an admin,
   * and registers the session
   * with the session registry.
   *
   * @param destination the destination topic for the chatroom subscription
   * @param token the JWT token for authentication
   * @param sessionId the session ID of the WebSocket connection
   * @param username the username of the user subscribing to the chatroom
   */
  private void handleChatroomSubscription(String destination, String token, String sessionId, String username) {
    String requestId = destination.replace("/topic/chatroom/", "");
    try {
      jwtUtil.validateChatroomToken(token, requestId);
      String role = jwtUtil.extractRole(token);
      if (role.equals(AdminRole.SUPERUSER.toString()) || role.equals(AdminRole.MODERATOR.toString())) {
        userRequestService.tryLockRequest(requestId, username);
        sessionRegistry.registerSession(
            new SessionInfo(sessionId, username, new HashSet<>(Collections.singleton(WebsocketRole.ADMIN_REQUEST_CHATTER)), requestId)
        );
      } else {
        sessionRegistry.registerSession(
            new SessionInfo(sessionId, username, new HashSet<>(Collections.singleton(WebsocketRole.ANONYMOUS_USER)), requestId)
        );
      }
      LogHelper.info(logger, "User {0} subscribed to chatroom {1}", username, requestId);
    } catch (Exception e) {
      LogHelper.severe(logger, "Failed to lock request {0} for admin {1}: {2}", requestId, username, e.getMessage());
      webSocketNotificationHandler.sendInfo(username,
          "Obs! This request is currently being handled by another administrator",
          "notification.request.locked");
      sessionRegistry.registerSession(
          new SessionInfo(sessionId, username, new HashSet<>(Collections.singleton(WebsocketRole.ADMIN_REQUEST_CHATTER)), requestId)
      );
    }
  }

  /**
   * Handles subscription to personal notifications for admins.
   * Validates the JWT token and checks if the user is a superuser or moderator.
   * If so, registers the session for personal notifications.
   * If not, logs a warning and does not register the session.
   *
   * @param sessionId the session ID of the WebSocket connection
   * @param userId the ID of the user subscribing to personal notifications
   * @param token the JWT token for authentication
   */
  private void handleAdminPersonalNotificationSubscription(String sessionId, String userId, String token) {
    String role = jwtUtil.extractRole(token);
    if (role.equals(AdminRole.SUPERUSER.toString()) || role.equals(AdminRole.MODERATOR.toString())) {
      sessionRegistry.registerSession(
          new SessionInfo(sessionId, userId, new HashSet<>(Collections.singleton(WebsocketRole.ADMIN_PERSONAL_OBSERVER)), null)
      );
      LogHelper.info(logger, "User {0} is a superuser or moderator, subscribing to personal notifications", userId);
    } else {
      LogHelper.warning(logger, "User {0} is not a superuser or moderator, and is blocked from subscribing to personal notifications", userId);
    }
  }

  /**
   * Handles subscription to the error stream for admins.
   * Validates the JWT token and checks if the user is a superuser or moderator.
   * If so, registers the session for error stream notifications.
   * If not, logs a warning and does not register the session.
   *
   * @param sessionId the session ID of the WebSocket connection
   * @param userId the ID of the user subscribing to the error stream
   * @param token the JWT token for authentication
   */
  private void handleErrorStreamSubscription(String sessionId, String userId, String token) {
    String role = jwtUtil.extractRole(token);
    if (role.equals(AdminRole.SUPERUSER.toString()) || role.equals(AdminRole.MODERATOR.toString())) {
      sessionRegistry.registerSession(
          new SessionInfo(sessionId, userId, new HashSet<>(Collections.singleton(WebsocketRole.ADMIN_PERSONAL_OBSERVER)), null)
      );
      LogHelper.info(logger, "User {0} is a superuser or moderator, subscribing to error stream", userId);
    } else {
      LogHelper.warning(logger, "User {0} is not a superuser or moderator, and is blocked from subscribing to error stream", userId);
    }
  }

  /**
   * Handles subscription to request notifications.
   * Validates the JWT token,
   * determines the user's role based on the request ID,
   * and registers the session
   * with the session registry.
   * If the user is an anonymous user,
   * sends a user logged-in notification.
   * If the user is an admin observer,
   * sends a request currently under review notification.
   * 
   * @param destination the destination topic for the request notifications
   * @param token the JWT token for authentication
   * @param sessionId the session ID of the WebSocket connection
   */
  private void handleRequestNotificationSubscription(String destination, String token, String sessionId) {
    String requestId = destination.replace("/topic/request/", "");
    try {
      jwtUtil.validateChatroomToken(token, requestId);
      String userId = jwtUtil.extractUsername(token);
      WebsocketRole role = userId.equals(requestId)
          ? WebsocketRole.ANONYMOUS_USER
          : WebsocketRole.ADMIN_REQUEST_OBSERVER;

      if (role == WebsocketRole.ANONYMOUS_USER) {
        webSocketNotificationHandler.sendRequestUpdateToObservers(requestId, WebsocketNotificationType.USER_LOGGED_IN);
      } else {
        webSocketNotificationHandler.sendRequestUpdateToObservers(requestId, WebsocketNotificationType.REQUEST_CURRENTLY_UNDER_REVIEW);
      }

      sessionRegistry.registerSession(
          new SessionInfo(sessionId, userId, new HashSet<>(Collections.singleton(role)), requestId)
      );
      LogHelper.info(logger, "User {0} subscribed to request notifications", userId);
    } catch (Exception e) {
      LogHelper.severe(logger, "Failed to subscribe to request {0}: {1}", requestId, e.getMessage());
    }
  }
}
