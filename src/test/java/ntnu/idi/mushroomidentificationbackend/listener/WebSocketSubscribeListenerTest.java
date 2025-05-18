package ntnu.idi.mushroomidentificationbackend.listener;

import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketNotificationType;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

import java.util.Map;

import static org.mockito.Mockito.*;

class WebSocketSubscribeListenerTest {

  private JWTUtil jwtUtil;
  private UserRequestService userRequestService;
  private WebSocketNotificationHandler notificationHandler;
  private SessionRegistry sessionRegistry;
  private WebSocketSubscribeListener listener;

  @BeforeEach
  void setUp() {
    jwtUtil = mock(JWTUtil.class);
    userRequestService = mock(UserRequestService.class);
    notificationHandler = mock(WebSocketNotificationHandler.class);
    sessionRegistry = mock(SessionRegistry.class);
    listener = new WebSocketSubscribeListener(jwtUtil, userRequestService, notificationHandler, sessionRegistry);
  }

  @Test
  void handleSubscribe_withMissingToken_doesNothing() {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setDestination("/topic/chatroom/req1");
    accessor.setSessionId("session1");
    Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
    SessionSubscribeEvent event = new SessionSubscribeEvent(this, message);

    listener.handleSubscribe(event);
    verifyNoInteractions(jwtUtil);
  }

  @Test
  void handleSubscribe_withInvalidToken_doesNothing() {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setDestination("/topic/chatroom/req1");
    accessor.setSessionId("session1");
    accessor.setNativeHeader("Authorization", "Bearer invalid");
    Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
    SessionSubscribeEvent event = new SessionSubscribeEvent(this, message);

    when(jwtUtil.isTokenValid("invalid")).thenReturn(false);

    listener.handleSubscribe(event);
    verify(jwtUtil).isTokenValid("invalid");
  }

  @Test
  void handleSubscribe_toUnhandledDestination_logsWarning() {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setDestination("/unhandled/topic");
    accessor.setSessionId("s1");
    accessor.setNativeHeader("Authorization", "Bearer token");
    Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
    SessionSubscribeEvent event = new SessionSubscribeEvent(this, message);

    when(jwtUtil.isTokenValid("token")).thenReturn(true);
    when(jwtUtil.extractUsername("token")).thenReturn("user1");

    listener.handleSubscribe(event);
  }

  @Test
  void handleSubscribe_toRequest_withException_stillRegistersSession() {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setDestination("/topic/chatroom/req1");
    accessor.setSessionId("s1");
    accessor.setNativeHeader("Authorization", "Bearer token");
    Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
    SessionSubscribeEvent event = new SessionSubscribeEvent(this, message);

    when(jwtUtil.isTokenValid("token")).thenReturn(true);
    when(jwtUtil.extractUsername("token")).thenReturn("admin");
    when(jwtUtil.extractRole("token")).thenReturn("SUPERUSER");
    doThrow(new RuntimeException("lock fail")).when(userRequestService).tryLockRequest("req1", "admin");

    listener.handleSubscribe(event);
    verify(notificationHandler).sendInfo(eq("admin"), anyString(), eq("notification.request.locked"));
    verify(sessionRegistry).registerSession(any());
  }

  @Test
  void handleSubscribe_toRequestNotification_registersCorrectly() {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setDestination("/topic/request/user1");
    accessor.setSessionId("s2");
    accessor.setNativeHeader("Authorization", "Bearer token");
    Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());
    SessionSubscribeEvent event = new SessionSubscribeEvent(this, message);

    when(jwtUtil.isTokenValid("token")).thenReturn(true);
    when(jwtUtil.extractUsername("token")).thenReturn("user1");

    listener.handleSubscribe(event);
    verify(notificationHandler).sendRequestUpdateToObservers("user1", WebsocketNotificationType.USER_LOGGED_IN);
    verify(sessionRegistry).registerSession(any());
  }
}
