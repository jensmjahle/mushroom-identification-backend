package ntnu.idi.mushroomidentificationbackend.listener;

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

@Component
@RequiredArgsConstructor
public class WebSocketSubscribeListener {

  private final JWTUtil jwtUtil;
  private final UserRequestService userRequestService;
  private final WebSocketNotificationHandler webSocketNotificationHandler;
  private final SessionRegistry sessionRegistry;
  private static final Logger logger = Logger.getLogger(WebSocketSubscribeListener.class.getName());

  @EventListener
  public void handleSubscribe(SessionSubscribeEvent event) {
    StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
    String sessionId = accessor.getSessionId();
    String destination = accessor.getDestination();
    String token = accessor.getFirstNativeHeader("Authorization");
    
    if(destination == null) return;

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
        handleAdminChannelSubscription(sessionId, username);
      } else if (destination.equals("/topic/notifications/" + username)) {
        handleAdminPersonalNotificationSubscription(sessionId, username, token);
      } else if (destination.equals("/topic/errors/" + username)) {
        handleErrorStreamSubscription(sessionId, username, token);
      } else if (destination.startsWith("/topic/request/")) {
        handleRequestNotificationSubscription(destination, token, sessionId);
      } else {
        LogHelper.warning(logger, "Unhandled subscription destination for user {0}: {1}",username, destination);
      }
    } catch (Exception e) {
      LogHelper.severe(logger, "Exception during subscription handling for session {0}: {1}", sessionId, e.getMessage());
    }
    
  }

  private void handleChatroomSubscription(String destination, String token, String sessionId, String username) {
    String requestId = destination.replace("/topic/chatroom/", "");
    try {
      jwtUtil.validateChatroomToken(token, requestId);
      String role = jwtUtil.extractRole(token);
      if (role.equals(AdminRole.SUPERUSER.toString()) || role.equals(AdminRole.MODERATOR.toString())) {
        userRequestService.tryLockRequest(requestId, username);
        sessionRegistry.registerSession(new SessionInfo(sessionId, username, WebsocketRole.ADMIN_REQUEST_OWNER, requestId));
      } else {
        sessionRegistry.registerSession(new SessionInfo(sessionId, username, WebsocketRole.ANONYMOUS_USER, requestId));
      }
      LogHelper.info(logger, "User {0} subscribed to chatroom {1}", username, requestId);
    } catch (Exception e) {
      LogHelper.severe(logger, "Failed to lock request {0} for admin {1}: {2}", requestId, username, e.getMessage());
      webSocketNotificationHandler.sendInfo(username, "Obs! This request is currently being handled by another administrator", "notification.request.locked");
      sessionRegistry.registerSession(new SessionInfo(sessionId, username, WebsocketRole.ADMIN_REQUEST_OBSERVER, requestId));
    }
  }

  private void handleAdminChannelSubscription(String sessionId, String username) {
    sessionRegistry.registerSession(new SessionInfo(sessionId, username, WebsocketRole.ADMIN_GLOBAL_OBSERVER, null));
    LogHelper.info(logger, "Admin {0} subscribed to the global admin channel", username);
  }

  private void handleAdminPersonalNotificationSubscription(String sessionId, String userId, String token) {
    String role = jwtUtil.extractRole(token);
    if (role.equals(AdminRole.SUPERUSER.toString()) || role.equals(AdminRole.MODERATOR.toString())) {
      sessionRegistry.registerSession(new SessionInfo(sessionId, userId, WebsocketRole.ADMIN_PERSONAL_OBSERVER, null));
      LogHelper.info(logger, "User {0} is a superuser or moderator, subscribing to personal notifications", userId);
    } else {
      LogHelper.warning(logger, "User {0} is not a superuser or moderator, and is blocked from subscribing to personal notifications", userId);
    }
  }

  private void handleErrorStreamSubscription(String sessionId, String userId, String token) {
    String role = jwtUtil.extractRole(token);
    if (role.equals(AdminRole.SUPERUSER.toString()) || role.equals(AdminRole.MODERATOR.toString())) {
      sessionRegistry.registerSession(new SessionInfo(sessionId, userId, WebsocketRole.ADMIN_PERSONAL_OBSERVER, null));
      LogHelper.info(logger, "User {0} is a superuser or moderator, subscribing to error stream", userId);
    } else {
      LogHelper.warning(logger, "User {0} is not a superuser or moderator, and is blocked from subscribing to error stream", userId);
    }
  }
  
  private void handleRequestNotificationSubscription(String destination, String token, String sessionId) {
    String requestId = destination.replace("/topic/request/", "");
    try {
      jwtUtil.validateChatroomToken(token, requestId);
     // token = token.replace("Bearer ", "");
      System.out.println("Token: " + token);
      WebsocketRole role;
      String userId = jwtUtil.extractUsername(token);
      if (userId.equals(requestId)) {
       role = WebsocketRole.ANONYMOUS_USER;
       webSocketNotificationHandler.sendRequestUpdateToObservers(requestId, WebsocketNotificationType.USER_LOGGED_IN);
      } else {
        role = WebsocketRole.ADMIN_REQUEST_OBSERVER;
        webSocketNotificationHandler.sendRequestUpdateToObservers(requestId, WebsocketNotificationType.REQUEST_CURRENTLY_UNDER_REVIEW);
      }
    sessionRegistry.registerSession(new SessionInfo(sessionId, userId, role, requestId));
    LogHelper.info(logger, "User {0} subscribed to request notifications", userId);
  } catch (Exception e) {
      LogHelper.severe(logger, "Failed to lock request {0} for user {1}: {2}", requestId, sessionId, e.getMessage());
    }
  }
}
