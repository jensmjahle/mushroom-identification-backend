package ntnu.idi.mushroomidentificationbackend.listener;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketRole;
import ntnu.idi.mushroomidentificationbackend.model.websocket.SessionInfo;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

class WebSocketDisconnectListenerTest {

  private SessionRegistry sessionRegistry;
  private UserRequestService userRequestService;
  private WebSocketNotificationHandler webSocketNotificationHandler;
  private WebSocketDisconnectListener listener;

  @BeforeEach
  void setUp() {
    sessionRegistry = mock(SessionRegistry.class);
    userRequestService = mock(UserRequestService.class);
    webSocketNotificationHandler = mock(WebSocketNotificationHandler.class);
    listener = new WebSocketDisconnectListener(sessionRegistry, userRequestService, webSocketNotificationHandler);
  }

  @Test
  void handleDisconnect_withAnonymousUserRole_triggersUserLoggedOut() {
    String sessionId = "s1";
    SessionInfo info = new SessionInfo(sessionId, "user1", Set.of(WebsocketRole.ANONYMOUS_USER), "req123");

    StompHeaderAccessor accessor = StompHeaderAccessor.create(StompCommand.DISCONNECT);
    accessor.setSessionId(sessionId);
    SessionDisconnectEvent event = new SessionDisconnectEvent(
        this,
        new GenericMessage<>(new byte[0], accessor.getMessageHeaders()),
        sessionId,
        null
    );

    when(sessionRegistry.getSessionInfo(sessionId)).thenReturn(Optional.empty());

    listener.handleDisconnect(event);

    verifyNoInteractions(userRequestService);
    verifyNoInteractions(webSocketNotificationHandler);
    verify(sessionRegistry, never()).unregisterSession(sessionId);
  }
}
