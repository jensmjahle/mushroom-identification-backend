package ntnu.idi.mushroomidentificationbackend.security;

import ntnu.idi.mushroomidentificationbackend.model.websocket.StompPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WebSocketAuthInterceptorTest {

  private JWTUtil jwtUtil;
  private WebSocketAuthInterceptor interceptor;

  @BeforeEach
  void setUp() {
    jwtUtil = mock(JWTUtil.class);
    interceptor = new WebSocketAuthInterceptor(jwtUtil);
  }

  @Test
  void preSend_invalidToken_returnsNull() {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.CONNECT);
    accessor.setNativeHeader("Authorization", "Bearer invalid-token");
    Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());

    when(jwtUtil.isTokenValid("invalid-token")).thenReturn(false);

    Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));
    assertNull(result);
  }
  
  @Test
  void preSend_chatSubscription_withInvalidToken_returnsNull() {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setDestination("/topic/chatroom/req123");
    accessor.setNativeHeader("Authorization", "Bearer token");
    Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());

    when(jwtUtil.isTokenValid("token")).thenReturn(true);
    when(jwtUtil.extractUsername("token")).thenReturn("user1");
    when(jwtUtil.extractRole("token")).thenReturn("USER");
    doThrow(new RuntimeException("unauthorized")).when(jwtUtil).validateChatroomToken("token", "req123");

    Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));
    assertNull(result);
  }

  @Test
  void preSend_adminsTopic_withNonAdminRole_returnsNull() {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setDestination("/topic/admins");
    accessor.setNativeHeader("Authorization", "Bearer token");
    Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());

    when(jwtUtil.isTokenValid("token")).thenReturn(true);
    when(jwtUtil.extractUsername("token")).thenReturn("user1");
    when(jwtUtil.extractRole("token")).thenReturn("USER");

    Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));
    assertNull(result);
  }

  @Test
  void preSend_subscribeToNotification_wrongUser_returnsNull() {
    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.SUBSCRIBE);
    accessor.setDestination("/topic/notifications/anotherUser");
    accessor.setNativeHeader("Authorization", "Bearer token");
    Message<byte[]> message = new GenericMessage<>(new byte[0], accessor.getMessageHeaders());

    when(jwtUtil.isTokenValid("token")).thenReturn(true);
    when(jwtUtil.extractUsername("token")).thenReturn("user1");
    when(jwtUtil.extractRole("token")).thenReturn("USER");

    Message<?> result = interceptor.preSend(message, mock(MessageChannel.class));
    assertNull(result);
  }
}
