package ntnu.idi.mushroomidentificationbackend.controller.websocket;

import java.util.Date;
import ntnu.idi.mushroomidentificationbackend.dto.request.NewMessageDTO;
import ntnu.idi.mushroomidentificationbackend.dto.response.MessageDTO;
import ntnu.idi.mushroomidentificationbackend.handler.SessionRegistry;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketErrorHandler;
import ntnu.idi.mushroomidentificationbackend.handler.WebSocketNotificationHandler;
import ntnu.idi.mushroomidentificationbackend.model.enums.WebsocketNotificationType;
import ntnu.idi.mushroomidentificationbackend.security.JWTUtil;
import ntnu.idi.mushroomidentificationbackend.security.SecurityConfigDev;
import ntnu.idi.mushroomidentificationbackend.service.MessageService;
import ntnu.idi.mushroomidentificationbackend.service.UserRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import static org.mockito.Mockito.*;

@WebMvcTest
@ActiveProfiles("dev")
@ContextConfiguration(classes = {
    ChatWebSocketController.class,
    ChatWebSocketControllerTest.TestConfig.class,
    SecurityConfigDev.class
})
class ChatWebSocketControllerTest {

  @Configuration
  static class TestConfig {
    @Bean public SimpMessagingTemplate messagingTemplate() { return mock(SimpMessagingTemplate.class); }
    @Bean public MessageService messageService() { return mock(MessageService.class); }
    @Bean public UserRequestService userRequestService() { return mock(UserRequestService.class); }
    @Bean public JWTUtil jwtUtil() { return mock(JWTUtil.class); }
    @Bean public WebSocketErrorHandler webSocketErrorHandler() { return mock(WebSocketErrorHandler.class); }
    @Bean public WebSocketNotificationHandler webSocketNotificationHandler() { return mock(WebSocketNotificationHandler.class); }
    @Bean public SessionRegistry sessionRegistry() { return mock(SessionRegistry.class); }
  }

  @Autowired
  private MessageService messageService;
  @Autowired
  private JWTUtil jwtUtil;
  @Autowired
  private SimpMessagingTemplate messagingTemplate;
  @Autowired
  private WebSocketNotificationHandler webSocketNotificationHandler;
  @Autowired
  private UserRequestService userRequestService;

  @Test
  void handleMessage_validInput_sendsMessageAndNotifiesObservers() throws Exception {
    ChatWebSocketController controller = new ChatWebSocketController(
        messagingTemplate, messageService, userRequestService, jwtUtil,
        mock(WebSocketErrorHandler.class), webSocketNotificationHandler, mock(SessionRegistry.class)
    );

    NewMessageDTO dto = new NewMessageDTO();
    dto.setContent("hello");

    MessageDTO response = new MessageDTO(null, ntnu.idi.mushroomidentificationbackend.model.enums.MessageSenderType.USER, "hello", new Date());

    when(jwtUtil.extractUsername(anyString())).thenReturn("user1");
    when(jwtUtil.extractRole(anyString())).thenReturn("MODERATOR");
    doNothing().when(jwtUtil).validateChatroomToken(anyString(), anyString());
    when(messageService.saveMessage(any(), any())).thenReturn(response);

    controller.handleMessage("req123", "Bearer token123", "session1", dto);

    verify(messageService).saveMessage(dto, "req123");
    verify(userRequestService).updateProjectAfterMessage("req123", response.getSenderType());
    verify(messagingTemplate).convertAndSend(eq("/topic/chatroom/req123"), eq(response));
    verify(webSocketNotificationHandler).sendRequestUpdateToObservers("req123", WebsocketNotificationType.NEW_CHAT_MESSAGE);
  }
}
